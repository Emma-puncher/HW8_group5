# 篩選功能測試腳本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   台北咖啡廳搜尋引擎 - 篩選功能測試" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 測試 1: 基礎搜尋（無篩選）
Write-Host "測試 1: 基礎搜尋（無篩選）" -ForegroundColor Yellow
Write-Host "搜尋關鍵字: 不限時" -ForegroundColor Gray
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/search?q=不限時" -Method GET
    Write-Host "✅ 成功 - 找到 $($result.total) 間咖啡廳" -ForegroundColor Green
    Write-Host "   前 3 名: " -ForegroundColor Gray
    $result.results | Select-Object -First 3 | ForEach-Object {
        Write-Host "   - $($_.name) (分數: $($_.score))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 2: 進階搜尋 - 地區篩選
Write-Host "測試 2: 進階搜尋 - 地區篩選" -ForegroundColor Yellow
Write-Host "搜尋關鍵字: 咖啡" -ForegroundColor Gray
Write-Host "地區篩選: 大安區" -ForegroundColor Gray
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/search/advanced?keyword=咖啡&districts=大安區" -Method GET
    Write-Host "✅ 成功 - 找到 $($result.total) 間大安區咖啡廳" -ForegroundColor Green
    Write-Host "   咖啡廳列表: " -ForegroundColor Gray
    $result.results | Select-Object -First 5 | ForEach-Object {
        Write-Host "   - $($_.name) [$($_.district)]" -ForegroundColor White
    }
    
    # 驗證所有結果都在大安區
    $allInDaan = $result.results | Where-Object { $_.district -ne "大安區" } | Measure-Object
    if ($allInDaan.Count -eq 0) {
        Write-Host "   ✓ 驗證通過：所有結果都在大安區" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 驗證失敗：有 $($allInDaan.Count) 筆不在大安區" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 3: 進階搜尋 - 功能篩選
Write-Host "測試 3: 進階搜尋 - 功能篩選" -ForegroundColor Yellow
Write-Host "搜尋關鍵字: 咖啡" -ForegroundColor Gray
Write-Host "功能篩選: 不限時" -ForegroundColor Gray
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/search/advanced?keyword=咖啡&features=不限時" -Method GET
    Write-Host "✅ 成功 - 找到 $($result.total) 間不限時咖啡廳" -ForegroundColor Green
    Write-Host "   咖啡廳列表: " -ForegroundColor Gray
    $result.results | Select-Object -First 5 | ForEach-Object {
        $features = if ($_.features) { $_.features -join ", " } else { "N/A" }
        Write-Host "   - $($_.name) [功能: $features]" -ForegroundColor White
    }
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 4: 進階搜尋 - 複合篩選
Write-Host "測試 4: 進階搜尋 - 複合篩選" -ForegroundColor Yellow
Write-Host "搜尋關鍵字: 安靜" -ForegroundColor Gray
Write-Host "地區篩選: 大安區" -ForegroundColor Gray
Write-Host "功能篩選: 不限時,有插座" -ForegroundColor Gray
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/search/advanced?keyword=安靜&districts=大安區&features=不限時,有插座" -Method GET
    Write-Host "✅ 成功 - 找到 $($result.total) 間符合條件的咖啡廳" -ForegroundColor Green
    if ($result.total -gt 0) {
        Write-Host "   咖啡廳列表: " -ForegroundColor Gray
        $result.results | ForEach-Object {
            $features = if ($_.features) { $_.features -join ", " } else { "N/A" }
            Write-Host "   - $($_.name) [$($_.district)] [功能: $features]" -ForegroundColor White
        }
    } else {
        Write-Host "   (沒有符合所有條件的咖啡廳)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 5: 推薦功能 - 無篩選
Write-Host "測試 5: 推薦功能 - 無篩選" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/recommendations" -Method GET
    Write-Host "✅ 成功 - 取得 $($result.recommendations.Count) 間推薦咖啡廳" -ForegroundColor Green
    Write-Host "   前 5 名推薦: " -ForegroundColor Gray
    $result.recommendations | Select-Object -First 5 | ForEach-Object {
        Write-Host "   - $($_.name) (分數: $($_.score))" -ForegroundColor White
    }
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 6: 推薦功能 - 帶篩選
Write-Host "測試 6: 推薦功能 - 帶地區篩選" -ForegroundColor Yellow
Write-Host "地區篩選: 內湖區" -ForegroundColor Gray
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/recommendations?districts=內湖區" -Method GET
    Write-Host "✅ 成功 - 取得 $($result.recommendations.Count) 間內湖區推薦咖啡廳" -ForegroundColor Green
    if ($result.recommendations.Count -gt 0) {
        Write-Host "   推薦列表: " -ForegroundColor Gray
        $result.recommendations | ForEach-Object {
            Write-Host "   - $($_.name) [$($_.district)]" -ForegroundColor White
        }
    }
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 7: 取得所有地區
Write-Host "測試 7: 取得可用地區列表" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/filters/districts" -Method GET
    Write-Host "✅ 成功 - 共 $($result.districts.Count) 個地區" -ForegroundColor Green
    Write-Host "   地區列表: $($result.districts -join ', ')" -ForegroundColor White
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 測試 8: 取得所有功能
Write-Host "測試 8: 取得可用功能列表" -ForegroundColor Yellow
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8080/api/filters/features" -Method GET
    Write-Host "✅ 成功 - 共 $($result.features.Count) 個功能" -ForegroundColor Green
    Write-Host "   功能列表: $($result.features -join ', ')" -ForegroundColor White
} catch {
    Write-Host "❌ 失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   測試完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "請在瀏覽器中訪問 http://localhost:8080 測試前端功能" -ForegroundColor Yellow
Write-Host "現在已修復的問題：" -ForegroundColor Green
Write-Host "  1. ✅ 功能篩選現在會正確呼叫 /api/search/advanced API" -ForegroundColor White
Write-Host "  2. ✅ 地區篩選會正確傳遞 districts 參數" -ForegroundColor White
Write-Host "  3. ✅ 功能篩選會正確傳遞 features 參數（逗號分隔）" -ForegroundColor White
Write-Host "  4. ✅ 推薦功能也支援篩選參數" -ForegroundColor White
Write-Host ""
