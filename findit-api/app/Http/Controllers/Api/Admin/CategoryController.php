<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\ActivityLog;
use App\Models\Category;
use Illuminate\Http\Request;

class CategoryController extends Controller
{
    public function publicIndex()
    {
        $categories = Category::orderBy('name')->get();

        return response()->json([
            'success' => true,
            'message' => 'Daftar kategori berhasil diambil.',
            'data' => $categories,
        ]);
    }

    public function index()
    {
        $categories = Category::withCount('items')
            ->orderBy('name')
            ->get();

        return response()->json([
            'success' => true,
            'message' => 'Daftar kategori admin berhasil diambil.',
            'data' => $categories,
        ]);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:100', 'unique:categories,name'],
        ]);

        $category = Category::create([
            'name' => $validated['name'],
        ]);

        $this->writeLog(
            $request,
            'CREATE_CATEGORY',
            'Admin menambahkan kategori: ' . $category->name
        );

        return response()->json([
            'success' => true,
            'message' => 'Kategori berhasil ditambahkan.',
            'data' => $category,
        ], 201);
    }

    public function update(Request $request, Category $category)
    {
        $validated = $request->validate([
            'name' => ['required', 'string', 'max:100', 'unique:categories,name,' . $category->id],
        ]);

        $oldName = $category->name;

        $category->update([
            'name' => $validated['name'],
        ]);

        $this->writeLog(
            $request,
            'UPDATE_CATEGORY',
            'Admin mengubah kategori dari ' . $oldName . ' menjadi ' . $category->name
        );

        return response()->json([
            'success' => true,
            'message' => 'Kategori berhasil diperbarui.',
            'data' => $category,
        ]);
    }

    public function destroy(Request $request, Category $category)
    {
        if ($category->items()->exists()) {
            return response()->json([
                'success' => false,
                'message' => 'Kategori tidak bisa dihapus karena masih digunakan oleh data barang.',
            ], 422);
        }

        $categoryName = $category->name;

        $category->delete();

        $this->writeLog(
            $request,
            'DELETE_CATEGORY',
            'Admin menghapus kategori: ' . $categoryName
        );

        return response()->json([
            'success' => true,
            'message' => 'Kategori berhasil dihapus.',
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
