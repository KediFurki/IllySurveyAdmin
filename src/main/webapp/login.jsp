<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Illy - Admin Login</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/css/uikit.min.css" />
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit-icons.min.js"></script>
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        html, body {
            height: 100%;
            width: 100%;
            overflow-x: hidden;
        }

        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            background-attachment: fixed;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            display: flex;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
        }

        :root {
            --illy-red: #d9381e;
            --illy-dark: #1f1f1f;
            --illy-dark-red: #b82f18;
        }

        .login-container {
            width: 100%;
            max-width: 450px;
            padding: 20px;
        }

        .login-card {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
            animation: slideInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .login-header {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: white;
            padding: 3rem 2rem;
            text-align: center;
        }

        .login-logo {
            font-size: 4rem;
            margin-bottom: 1rem;
            animation: scaleIn 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s backwards;
        }

        .login-title {
            font-size: 1.8rem;
            font-weight: 700;
            margin-bottom: 0.5rem;
            letter-spacing: -0.5px;
        }

        .login-subtitle {
            font-size: 0.95rem;
            opacity: 0.9;
            font-weight: 500;
        }

        .login-body {
            padding: 3rem 2rem;
        }

        .login-description {
            text-align: center;
            color: #666;
            font-size: 1rem;
            margin-bottom: 2rem;
            line-height: 1.6;
        }

        .login-description strong {
            color: var(--illy-red);
            font-weight: 700;
        }

        .btn-login {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            border: none;
            border-radius: 10px;
            padding: 16px 32px;
            font-weight: 700;
            color: white;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 8px 24px rgba(217, 56, 30, 0.3);
            width: 100%;
            font-size: 1.05rem;
            text-decoration: none;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 10px;
            position: relative;
            overflow: hidden;
        }

        .btn-login::before {
            content: '';
            position: absolute;
            top: 50%;
            left: 50%;
            width: 0;
            height: 0;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.3);
            transform: translate(-50%, -50%);
            transition: width 0.6s, height 0.6s;
        }

        .btn-login:hover::before {
            width: 300px;
            height: 300px;
        }

        .btn-login:hover {
            transform: translateY(-3px);
            box-shadow: 0 12px 32px rgba(217, 56, 30, 0.4);
        }

        .btn-login:active {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(217, 56, 30, 0.2);
        }

        .login-footer {
            border-top: 1px solid #f0f0f0;
            padding: 1.5rem 2rem;
            text-align: center;
            color: #999;
            font-size: 0.85rem;
        }

        .security-badge {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            color: var(--illy-red);
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .info-box {
            background: rgba(217, 56, 30, 0.05);
            border-left: 4px solid var(--illy-red);
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1.5rem;
            animation: slideIn 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.4s backwards;
        }

        .info-box-title {
            color: var(--illy-red);
            font-weight: 700;
            margin-bottom: 0.5rem;
            font-size: 0.9rem;
        }

        .info-box-text {
            color: #555;
            font-size: 0.9rem;
            line-height: 1.5;
        }

        .background-decoration {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: -1;
        }

        .shape {
            position: absolute;
            opacity: 0.1;
            animation: float 6s ease-in-out infinite;
        }

        .shape-1 {
            width: 300px;
            height: 300px;
            background: radial-gradient(circle, rgba(255,255,255,0.5) 0%, rgba(255,255,255,0) 70%);
            top: -100px;
            left: -100px;
            animation-delay: 0s;
        }

        .shape-2 {
            width: 200px;
            height: 200px;
            background: radial-gradient(circle, rgba(255,255,255,0.5) 0%, rgba(255,255,255,0) 70%);
            bottom: -50px;
            right: -50px;
            animation-delay: 2s;
        }

        .shape-3 {
            width: 150px;
            height: 150px;
            background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, rgba(255,255,255,0) 70%);
            top: 50%;
            right: 5%;
            animation-delay: 4s;
        }

        @keyframes slideInUp {
            from {
                opacity: 0;
                transform: translateY(50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateX(-30px);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
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

        @keyframes float {
            0%, 100% {
                transform: translateY(0px);
            }
            50% {
                transform: translateY(20px);
            }
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
            }
            to {
                opacity: 1;
            }
        }

        .login-card {
            animation: slideInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1), fadeIn 0.8s ease-out;
        }

        /* Responsive Design */
        @media (max-width: 640px) {
            .login-container {
                padding: 1rem;
            }

            .login-header {
                padding: 2rem 1.5rem;
            }

            .login-body {
                padding: 2rem 1.5rem;
            }

            .login-logo {
                font-size: 3rem;
            }

            .login-title {
                font-size: 1.5rem;
            }

            .btn-login {
                padding: 14px 24px;
                font-size: 1rem;
            }
        }
    </style>
</head>
<body>
    <!-- Background Decoration -->
    <div class="background-decoration">
        <div class="shape shape-1"></div>
        <div class="shape shape-2"></div>
        <div class="shape shape-3"></div>
    </div>

    <!-- Login Container -->
    <div class="login-container">
        <div class="login-card">
            <!-- Header -->
            <div class="login-header">
                <div class="login-logo">
                    <span uk-icon="icon: settings; ratio: 2"></span>
                </div>
                <div class="login-title">Illy Survey</div>
                <div class="login-subtitle">Admin Control Panel</div>
            </div>

            <!-- Body -->
            <div class="login-body">
                <!-- Security Badge -->
                <div class="security-badge">
                    <span uk-icon="icon: lock; ratio: 1.1"></span>
                    Accesso Sicuro
                </div>

                <!-- Description -->
                <div class="login-description">
                    Benvenuto nel portale di gestione sondaggi <strong>Illy</strong>. Accedi per visualizzare e gestire tutti i dati raccolti.
                </div>

                <!-- Info Box -->
                <div class="info-box">
                    <div class="info-box-title">
                        <span uk-icon="icon: info; ratio: 0.9"></span> Autenticazione
                    </div>
                    <div class="info-box-text">
                        Utilizza le tue credenziali Genesys per accedere al sistema. L'accesso è riservato al personale autorizzato.
                    </div>
                </div>

                <!-- Login Button -->
                <a href="login" class="btn-login">
                    <span uk-icon="icon: sign-in; ratio: 1.1"></span> Accedi con Genesys
                </a>
            </div>

            <!-- Footer -->
            <div class="login-footer">
                <div>© 2025 Illy Survey Admin</div>
                <div style="margin-top: 0.5rem;">
                    <span uk-icon="icon: shield; ratio: 0.8"></span> Sistema Sicuro e Protetto
                </div>
            </div>
        </div>
    </div>

    <script>
        // Add page load animation
        document.addEventListener('DOMContentLoaded', function() {
            // Animate button on hover
            const loginBtn = document.querySelector('.btn-login');
            if (loginBtn) {
                loginBtn.addEventListener('mouseenter', function() {
                    this.style.transform = 'translateY(-3px)';
                });
                loginBtn.addEventListener('mouseleave', function() {
                    this.style.transform = 'translateY(0)';
                });
            }
        });
    </script>

</body>
</html>