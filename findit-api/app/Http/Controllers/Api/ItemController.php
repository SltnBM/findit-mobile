<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Item;
use Illuminate\Http\Request;
use Carbon\Carbon;

class ItemController extends Controller
{
    public function index(Request $request)
    {
        $request->validate([
            'search' => ['nullable', 'string', 'max:100'],
            'category_id' => ['nullable', 'integer', 'exists:categories,id'],
            'category' => ['nullable', 'string', 'max:100'],
            'location' => ['nullable', 'string', 'max:100'],
            'date' => ['nullable', 'date'],
        ]);

        $query = Item::with('category')->where('status', 'published');

        if ($request->filled('search')) {
            $search = $request->search;
            $query->where(function ($q) use ($search) {
                $q->where('name', 'like', "%{$search}%")
                    ->orWhere('description', 'like', "%{$search}%")
                    ->orWhere('found_location', 'like', "%{$search}%")
                    ->orWhere('pickup_location', 'like', "%{$search}%")
                    ->orWhere('characteristics', 'like', "%{$search}%");
            });
        }

        if ($request->filled('category_id')) {
            $query->where('category_id', $request->category_id);
        }

        if ($request->filled('category')) {
            $category = $request->category;
            $query->whereHas('category', function ($q) use ($category) {
                $q->where('name', 'like', "%{$category}%");
            });
        }

        if ($request->filled('location')) {
            $location = $request->location;
            $query->where(function ($q) use ($location) {
                $q->where('found_location', 'like', "%{$location}%")
                    ->orWhere('pickup_location', 'like', "%{$location}%");
            });
        }

        if ($request->filled('date')) {
            $query->whereDate('found_date', $request->date);
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
            ];
        });

        return response()->json([
            'success' => true,
            'message' => 'Daftar barang published berhasil diambil.',
            'data' => $transformedItems,
        ]);
    }

    public function show(string $id)
    {
        $item = Item::with('category')
            ->where('status', 'published')
            ->find($id);

        if (!$item) {
            return response()->json([
                'success' => false,
                'message' => 'Barang tidak ditemukan atau belum dipublikasikan.',
            ], 404);
        }

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
        ];

        return response()->json([
            'success' => true,
            'message' => 'Detail barang berhasil diambil.',
            'data' => $data,
        ]);
    }
}
