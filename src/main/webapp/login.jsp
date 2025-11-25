<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Illy - Login</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/css/uikit.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit-icons.min.js"></script>
    
    <style>
        .illy-bg {
            background-color: #d9381e; /* Illy Kırmızısı */
        }
    </style>
</head>
<body>

    <div class="uk-section uk-section-muted uk-flex uk-flex-middle uk-animation-fade" uk-height-viewport>
        <div class="uk-width-1-1">
            <div class="uk-container">
                <div class="uk-grid-margin uk-grid uk-grid-stack" uk-grid>
                    <div class="uk-width-1-1@m">
                        <div class="uk-margin uk-width-large uk-margin-auto uk-card uk-card-default uk-card-body uk-box-shadow-large">
                            <div class="uk-text-center uk-margin-medium-bottom">
                                <h1 class="uk-heading-line uk-text-center"><span>illy</span></h1>
                                <p class="uk-text-meta">Survey Admin Panel</p>
                            </div>

                            <div class="uk-text-center">
                                <p>Benvenuto nel portale di gestione sondaggi.</p>
                                <a href="login" class="uk-button uk-button-danger uk-button-large uk-width-1-1 illy-bg">
                                    <span uk-icon="icon: sign-in; ratio: 1.2"></span> &nbsp; Accedi con Genesys
                                </a>
                            </div>
                            
                            <div class="uk-text-small uk-text-center uk-margin-top uk-text-muted">
                                Accesso riservato al personale autorizzato.
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</body>
</html>