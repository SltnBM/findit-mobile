<?php

namespace App\Http\Controllers\Api\Admin;

use App\Http\Controllers\Controller;
use App\Models\ActivityLog;
use Illuminate\Http\Request;

class ActivityLogController extends Controller
{
    public function index(Request $request)
    {
        $request->validate([
            'limit' => ['nullable', 'integer', 'min:1', 'max:200'],
        ]);

        $limit = $request->input('limit', 100);

        $logs = ActivityLog::with('user:id,name,email,role')
            ->latest('created_at')
            ->limit($limit)
            ->get();

        return response()->json([
            'success' => true,
            'message' => 'Activity log berhasil diambil.',
            'data' => $logs,
        ]);
    }
}
