<?php

namespace Database\Seeders;

use App\Models\Category;
use Illuminate\Database\Seeder;

class CategorySeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        $categories = [
            'Elektronik',
            'Dokumen',
            'Aksesoris',
            'Pakaian',
            'Alat Tulis',
            'Tas',
            'Buku',
            'Lain-lain',
        ];

        foreach ($categories as $category) {
            Category::updateOrCreate([
                'name' => $category,
            ]);
        }
    }
}
