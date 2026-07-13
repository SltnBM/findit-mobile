<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\ActivityLog;
use App\Models\Item;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;
use Carbon\Carbon;

class AdminItemController extends Controller
{
    public function index(Request $request)
    {
        $request->validate([
            'status' => ['nullable', 'in:draft,published,closed'],
            'search' => ['nullable', 'string', 'max:100'],
        ]);

        $query = Item::with(['category', 'creator:id,name,email,role', 'closer:id,name,email,role']);

        if ($request->filled('status')) {
            $query->where('status', $request->status);
        }

        if ($request->filled('search')) {
            $search = $request->search;

            $query->where(function ($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                    ->orWhere('description', 'like', "%{$search}%")
                    ->orWhere('found_location', 'like', "%{$search}%")
                    ->orWhere('pickup_location', 'like', "%{$search}%");
            });
        }

        $items = $query->latest()->get();

        $transformedItems = $items->map(function ($item) {
            return [
                'id' => $item->id,
                'category_id' => $item->category_id,
                'category' => $item->category,
                'name' => $item->name,
                'description' => $item->description,
                'photo' => $item->photo,
                'photo_url' => $item->photo ? url('storage/' . $item->photo) : null,
                'claim_photo_url' => $item->claim_photo ? url('storage/' . $item->claim_photo) : null,
                'found_location' => $item->found_location,

                'found_date' => $item->found_date ? Carbon::parse($item->found_date)->format('Y-m-d') : null,

                'pickup_location' => $item->pickup_location,
                'characteristics' => $item->characteristics,
                'admin_note' => $item->admin_note,
                'status' => $item->status,

                'published_at' => $item->published_at ? Carbon::parse($item->published_at)->format('Y-m-d H:i:s') : null,
                'closed_at' => $item->closed_at ? Carbon::parse($item->closed_at)->format('Y-m-d H:i:s') : null,

                'creator' => $item->creator,
                'closer' => $item->closer,
            ];
        });

        $counts = [
            'draft' => Item::where('status', 'draft')->count(),
            'published' => Item::where('status', 'published')->count(),
            'closed' => Item::where('status', 'closed')->count(),
        ];

        return response()->json([
            'success' => true,
            'message' => 'Daftar barang admin berhasil diambil.',
            'counts' => $counts,
            'data' => $transformedItems,
        ]);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'category_id' => ['required', 'integer', 'exists:categories,id'],
            'name' => ['required', 'string', 'max:150'],
            'description' => ['required', 'string'],
            'photo' => ['required', 'image', 'mimes:jpg,jpeg,png', 'max:2048'],
            'found_location' => ['required', 'string', 'max:150'],
            'found_date' => ['required', 'date'],
            'pickup_location' => ['required', 'string', 'max:150'],
            'characteristics' => ['nullable', 'string'],
            'admin_note' => ['nullable', 'string'],
        ]);

        $photoPath = $request->file('photo')->store('items', 'public');

        $item = Item::create([
            'category_id' => $validated['category_id'],
            'name' => $validated['name'],
            'description' => $validated['description'],
            'photo' => $photoPath,
            'found_location' => $validated['found_location'],
            'found_date' => $validated['found_date'],
            'pickup_location' => $validated['pickup_location'],
            'characteristics' => $validated['characteristics'] ?? null,
            'admin_note' => $validated['admin_note'] ?? null,
            'status' => 'draft',
            'created_by' => $request->user()->id,
        ]);

        $item->load('category');

        $this->writeLog(
            $request,
            'CREATE_ITEM',
            'Admin menambahkan barang: ' . $item->name
        );

        return response()->json([
            'success' => true,
            'message' => 'Barang berhasil ditambahkan sebagai draft.',
            'data' => $item,
        ], 201);
    }

    public function show(Item $item)
    {
        $item->load(['category', 'creator:id,name,email,role', 'closer:id,name,email,role']);

        $data = [
            'id' => $item->id,
            'category_id' => $item->category_id,
            'category' => $item->category,
            'name' => $item->name,
            'description' => $item->description,
            'photo' => $item->photo,
            'photo_url' => $item->photo ? url('storage/' . $item->photo) : null,
            'claim_photo_url' => $item->claim_photo ? url('storage/' . $item->claim_photo) : null,
            'found_location' => $item->found_location,

            'found_date' => $item->found_date ? Carbon::parse($item->found_date)->format('Y-m-d') : null,

            'pickup_location' => $item->pickup_location,
            'characteristics' => $item->characteristics,
            'admin_note' => $item->admin_note,
            'status' => $item->status,
            'published_at' => $item->published_at ? Carbon::parse($item->published_at)->format('Y-m-d H:i:s') : null,
            'closed_at' => $item->closed_at ? Carbon::parse($item->closed_at)->format('Y-m-d H:i:s') : null,
            'creator' => $item->creator,
            'closer' => $item->closer,
        ];

        return response()->json([
            'success' => true,
            'message' => 'Detail barang admin berhasil diambil.',
            'data' => $data,
        ]);
    }

    public function update(Request $request, Item $item)
    {
        $validated = $request->validate([
            'category_id' => ['required', 'integer', 'exists:categories,id'],
            'name' => ['required', 'string', 'max:150'],
            'description' => ['required', 'string'],
            'photo' => ['nullable', 'image', 'mimes:jpg,jpeg,png', 'max:2048'],
            'found_location' => ['required', 'string', 'max:150'],
            'found_date' => ['required', 'date'],
            'pickup_location' => ['required', 'string', 'max:150'],
            'characteristics' => ['nullable', 'string'],
            'admin_note' => ['nullable', 'string'],
        ]);

        if ($request->hasFile('photo')) {
            if ($item->photo && Storage::disk('public')->exists($item->photo)) {
                Storage::disk('public')->delete($item->photo);
            }

            $validated['photo'] = $request->file('photo')->store('items', 'public');
        }

        $item->update($validated);
        $item->load('category');

        $this->writeLog(
            $request,
            'UPDATE_ITEM',
            'Admin mengedit barang: ' . $item->name
        );

        return response()->json([
            'success' => true,
            'message' => 'Barang berhasil diperbarui.',
            'data' => $item,
        ]);
    }

    public function destroy(Request $request, Item $item)
    {
        $itemName = $item->name;

        $item->delete();

        $this->writeLog(
            $request,
            'DELETE_ITEM',
            'Admin menghapus barang : ' . $itemName
        );

        return response()->json([
            'success' => true,
            'message' => 'Barang berhasil dihapus.',
        ]);
    }

    public function publish(Request $request, Item $item)
    {
        if ($item->status === 'closed') {
            return response()->json([
                'success' => false,
                'message' => 'Barang yang sudah closed tidak bisa dipublish ulang.',
            ], 422);
        }

        $item->update([
            'status' => 'published',
            'published_at' => now(),
        ]);

        $item->load('category');

        $this->writeLog(
            $request,
            'PUBLISH_ITEM',
            'Admin mempublish barang: ' . $item->name
        );

        return response()->json([
            'success' => true,
            'message' => 'Barang berhasil dipublish.',
            'data' => $item,
        ]);
    }

    public function close(Request $request, Item $item)
    {
        if ($item->status !== 'published') {
            return response()->json([
                'success' => false,
                'message' => 'Hanya barang berstatus published yang bisa ditutup.',
            ], 422);
        }

        $request->validate([
            'claim_photo' => ['required', 'image', 'mimes:jpeg,png,jpg', 'max:2048']
        ]);

        if ($request->hasFile('claim_photo')) {
            if ($item->claim_photo && Storage::disk('public')->exists($item->claim_photo)) {
                Storage::disk('public')->delete($item->claim_photo);
            }

            $path = $request->file('claim_photo')->store('claims', 'public');
            $item->claim_photo = $path;
        }

        $item->status = 'closed';
        $item->closed_by = $request->user()->id;
        $item->closed_at = now();
        $item->save();

        $item->load(['category', 'creator:id,name,email,role', 'closer:id,name,email,role']);

        $this->writeLog(
            $request,
            'CLOSE_ITEM',
            'Admin menutup barang karena sudah diambil: ' . $item->name
        );

        $data = [
            'id' => $item->id,
            'category_id' => $item->category_id,
            'category' => $item->category,
            'name' => $item->name,
            'description' => $item->description,
            'photo' => $item->photo,
            'photo_url' => $item->photo ? url('storage/' . $item->photo) : null,
            'claim_photo_url' => $item->claim_photo ? url('storage/' . $path) : null,
            'found_location' => $item->found_location,


            'found_date' => $item->found_date ? Carbon::parse($item->found_date)->format('Y-m-d') : null,

            'pickup_location' => $item->pickup_location,
            'characteristics' => $item->characteristics,
            'admin_note' => $item->admin_note,
            'status' => $item->status,
            'published_at' => $item->published_at ? Carbon::parse($item->published_at)->format('Y-m-d H:i:s') : null,
            'closed_at' => $item->closed_at ? Carbon::parse($item->closed_at)->format('Y-m-d H:i:s') : null,
            'creator' => $item->creator,
            'closer' => $item->closer,
        ];

        return response()->json([
            'success' => true,
            'message' => 'Barang berhasil diserahkan dan didokumentasikan.',
            'data' => $data,
        ]);
    }

    private function writeLog(Request $request, string $action, string $description): void
    {
        ActivityLog::create([
            'user_id' => $request->user()->id,
            'action' => $action,
            'description' => $description,
            'ip_address' => $request->ip(),
            'user_agent' => $request->userAgent(),
            'created_at' => now(),
        ]);
    }
}
