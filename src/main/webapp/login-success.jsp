<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Authentication Successful</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/css/uikit.min.css" />
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }

        .success-container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 3rem 2rem;
            text-align: center;
            max-width: 400px;
            width: 90%;
        }

        .success-icon {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, #d9381e 0%, #b82f18 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1.5rem;
            animation: scaleIn 0.5s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .success-icon svg {
            width: 50px;
            height: 50px;
            stroke: white;
            stroke-width: 3;
            fill: none;
            stroke-linecap: round;
            stroke-linejoin: round;
        }

        .success-title {
            font-size: 1.5rem;
            font-weight: 700;
            color: #1f1f1f;
            margin-bottom: 0.75rem;
        }

        .success-message {
            font-size: 1rem;
            color: #666;
            margin-bottom: 2rem;
        }

        .spinner {
            border: 3px solid #f3f3f3;
            border-top: 3px solid #d9381e;
            border-radius: 50%;
            width: 40px;
            height: 40px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        @keyframes scaleIn {
            from {
                opacity: 0;
                transform: scale(0.8);
            }
            to {
                opacity: 1;
                transform: scale(1);
            }
        }
    </style>
</head>
<body>
    <div class="success-container">
        <div class="success-icon">
            <svg viewBox="0 0 52 52">
                <polyline points="14 27 22 35 38 19"/>
            </svg>
        </div>
        <div class="success-title">Authentication Successful</div>
        <div class="success-message">Closing window and redirecting...</div>
        <div class="spinner"></div>
    </div>

    <script>
        // Check if this window was opened as a popup
        if (window.opener && !window.opener.closed) {
            // Popup scenario - use postMessage to signal parent window
            setTimeout(function() {
                try {
                    // Send message to parent window (safe for cross-origin)
                    window.opener.postMessage('LOGIN_SUCCESS', '*');
                    // Close the popup immediately after sending message
                    window.close();
                } catch (e) {
                    console.error('Error sending message to parent window:', e);
                    // Fallback: redirect this window
                    window.location.href = '<%= request.getContextPath() %>/admin';
                }
            }, 1000);
        } else {
            // Direct access scenario - redirect current window
            setTimeout(function() {
                window.location.href = '<%= request.getContextPath() %>/admin';
            }, 1000);
        }
    </script>
</body>
</html>
