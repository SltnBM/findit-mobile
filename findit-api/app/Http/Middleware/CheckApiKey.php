<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class CheckApiKey
{
    public function handle(Request $request, Closure $next): Response
    {
        if ($request->header('X-API-Key') !== 'findit_api_key_secret_123') {
            return response()->json([
                'success' => false,
                'message' => 'Unauthorized API Key'
            ], 401);
        }

        return $next($request);
    }
}
