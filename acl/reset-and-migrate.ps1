# ==============================================
# ACL Database Reset and Migration Script (PowerShell)
# ==============================================

Write-Host "==============================================" -ForegroundColor Green
Write-Host "ACL Database Reset and Migration Script" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

Write-Host ""
Write-Host "Step 1: Stopping ACL application..." -ForegroundColor Yellow
Write-Host "(Please stop the ACL application manually if running)" -ForegroundColor Yellow

Write-Host ""
Write-Host "Step 2: Clearing database..." -ForegroundColor Yellow

# Execute the reset SQL script
try {
    psql -h localhost -U postgres -d acl_db -f reset-database.sql
    Write-Host "Database cleared successfully!" -ForegroundColor Green
} catch {
    Write-Host "Error clearing database: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Step 3: Database cleared successfully!" -ForegroundColor Green
Write-Host ""
Write-Host "Step 4: Now you can start the ACL application" -ForegroundColor Cyan
Write-Host "The new V1__Create_complete_acl_system.sql migration will run automatically" -ForegroundColor Cyan
Write-Host ""
Write-Host "==============================================" -ForegroundColor Green
Write-Host "Reset Complete!" -ForegroundColor Green
Write-Host "==============================================" -ForegroundColor Green

Read-Host "Press Enter to continue"
