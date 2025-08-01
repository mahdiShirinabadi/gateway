@echo off
REM Generate RSA Keys for SSO Service
REM This script generates RSA private and public keys for JWT signing

echo Generating RSA keys for SSO service...

REM Create resources directory if it doesn't exist
if not exist "src\main\resources" mkdir "src\main\resources"

REM Generate private key
openssl genrsa -out src\main\resources\rsa_private.key 2048

REM Generate public key from private key
openssl rsa -in src\main\resources\rsa_private.key -pubout -out src\main\resources\rsa_public.key

echo RSA keys generated successfully!
echo Private key: src\main\resources\rsa_private.key
echo Public key: src\main\resources\rsa_public.key
echo.
echo Note: Keep the private key secure and never commit it to version control!
echo The public key can be shared for token validation.
pause 