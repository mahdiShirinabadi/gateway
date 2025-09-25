@echo off
echo ==============================================
echo ACL Database Reset and Migration Script
echo ==============================================

echo.
echo Step 1: Stopping ACL application...
echo (Please stop the ACL application manually if running)

echo.
echo Step 2: Clearing database...
psql -h localhost -U postgres -d acl_db -f reset-database.sql

echo.
echo Step 3: Database cleared successfully!
echo.
echo Step 4: Now you can start the ACL application
echo The new V1__Create_complete_acl_system.sql migration will run automatically
echo.
echo ==============================================
echo Reset Complete!
echo ==============================================
pause
