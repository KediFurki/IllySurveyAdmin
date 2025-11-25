<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<% 
    if (request.getAttribute("reportList") == null) {
        response.sendRedirect("admin");
        return;
    }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Illy - Report Sondaggi</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/css/uikit.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit-icons.min.js"></script>
    
    <style>
        .illy-red { color: #d9381e; }
        .illy-bg { background-color: #d9381e; color: white; }
    </style>
</head>
<body>

    <nav class="uk-navbar-container illy-bg" uk-navbar>
        <div class="uk-navbar-left">
            <a class="uk-navbar-item uk-logo uk-light" href="#" style="color:white;">illy Survey Admin</a>
        </div>
        <div class="uk-navbar-right">
             <div class="uk-navbar-item">
                <span uk-icon="icon: user"></span> &nbsp; Admin
            </div>
        </div>
    </nav>

    <div class="uk-container uk-margin-medium-top">
        
        <div class="uk-card uk-card-default uk-card-body uk-margin-bottom uk-box-shadow-small">
            <h3 class="uk-card-title illy-red"><span uk-icon="icon: settings"></span> Filtri</h3>
            
            <form action="admin" method="get" class="uk-grid-small" uk-grid>
                <div class="uk-width-1-3@s">
                    <label class="uk-form-label" for="start">Dal (Data Inizio)</label>
                    <div class="uk-form-controls">
                        <input class="uk-input" id="start" type="date" name="startDate" value="${startDate}">
                    </div>
                </div>
                <div class="uk-width-1-3@s">
                    <label class="uk-form-label" for="end">Al (Data Fine)</label>
                    <div class="uk-form-controls">
                        <input class="uk-input" id="end" type="date" name="endDate" value="${endDate}">
                    </div>
                </div>
                <div class="uk-width-1-3@s uk-flex uk-flex-bottom">
                    <button type="submit" class="uk-button uk-button-primary illy-bg uk-margin-small-right">
                        <span uk-icon="search"></span> Filtra
                    </button>
                    <a href="admin?action=export&startDate=${startDate}&endDate=${endDate}" class="uk-button uk-button-secondary">
                        <span uk-icon="download"></span> CSV
                    </a>
                </div>
            </form>
        </div>

        <div class="uk-card uk-card-default uk-card-body uk-box-shadow-medium">
            <table class="uk-table uk-table-divider uk-table-hover uk-table-middle uk-table-striped">
                <thead>
                    <tr>
                        <th class="uk-width-small">Data</th>
                        <th>ID Interazione</th>
                        <th>Telefono</th>
                        <th>Tipo</th>
                        <th>Voto</th>
                        <th>Audio</th>
                    </tr>
                </thead>
                <tbody>
                    <c:if test="${empty reportList}">
                        <tr><td colspan="6" class="uk-text-center uk-text-muted">Nessun dato trovato.</td></tr>
                    </c:if>

                    <c:forEach var="item" items="${reportList}">
                        <tr>
                            <td>${item.date}</td>
                            <td><span class="uk-text-meta">${item.conversationId}</span></td>
                            <td>${item.customerPhone}</td>
                            <td>
                                <span class="uk-label uk-label-warning">${item.type != null ? item.type : '-'}</span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.score < 6}">
                                        <span class="uk-badge" style="background-color: #d9381e;">${item.score}</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="uk-badge" style="background-color: #32d296;">${item.score}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${item.hasAudio == 'true'}">
                                    <span class="uk-icon-button" uk-icon="microphone" style="color: #d9381e;"></span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        
        <div class="uk-margin-large-bottom"></div>
    </div>

</body>
</html>