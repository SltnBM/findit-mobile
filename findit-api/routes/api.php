<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\ItemController;
use App\Http\Controllers\Api\Admin\AdminItemController;
use App\Http\Controllers\Api\Admin\CategoryController;
use App\Http\Controllers\Api\Admin\ActivityLogController;
use Illuminate\Support\Facades\Route;

Route::post('/login', [AuthController::class, 'login']);
Route::post('/register', [AuthController::class, 'register']);

Route::middleware('auth:sanctum')->group(function () {
    Route::post('/logout', [AuthController::class, 'logout']);
    Route::get('/profile', [AuthController::class, 'profile']);

    Route::get('/items', [ItemController::class, 'index']);
    Route::get('/items/{id}', [ItemController::class, 'show']);
    Route::get('/categories', [CategoryController::class, 'publicIndex']);

    Route::middleware('role:admin')->prefix('admin')->group(function () {
        Route::get('/items', [AdminItemController::class, 'index']);
        Route::post('/items', [AdminItemController::class, 'store']);
        Route::get('/items/{item}', [AdminItemController::class, 'show']);
        Route::post('/items/{item}', [AdminItemController::class, 'update']);
        Route::delete('/items/{item}', [AdminItemController::class, 'destroy']);
        Route::put('/items/{item}/publish', [AdminItemController::class, 'publish']);
        Route::post('/items/{item}/close', [AdminItemController::class, 'close']);

        Route::get('/categories', [CategoryController::class, 'index']);
        Route::post('/categories', [CategoryController::class, 'store']);
        Route::put('/categories/{category}', [CategoryController::class, 'update']);
        Route::delete('/categories/{category}', [CategoryController::class, 'destroy']);

        Route::get('/activity-logs', [ActivityLogController::class, 'index']);
    });
});
