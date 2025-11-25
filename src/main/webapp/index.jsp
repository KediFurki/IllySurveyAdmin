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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Illy - Report Sondaggi</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/css/uikit.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit-icons.min.js"></script>
    
    <style>
        * {
            margin: 0;
            padding: 0;
        }

        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            min-height: 100vh;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
        }

        /* Illy Brand Colors */
        :root {
            --illy-red: #d9381e;
            --illy-dark: #1f1f1f;
            --illy-light: #f8f8f8;
        }

        /* Navbar Styling */
        .illy-navbar {
            background: linear-gradient(135deg, var(--illy-red) 0%, #b82f18 100%);
            box-shadow: 0 4px 20px rgba(217, 56, 30, 0.2);
            padding: 1rem 2rem !important;
        }

        .illy-navbar .uk-logo {
            font-weight: 700;
            font-size: 1.5rem;
            letter-spacing: -0.5px;
        }

        /* Filter Card */
        .filter-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
            border: 1px solid rgba(217, 56, 30, 0.1);
            transition: all 0.3s ease;
        }

        .filter-card:hover {
            box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
        }

        .filter-card .uk-card-title {
            color: var(--illy-red);
            font-weight: 700;
            border-bottom: 2px solid var(--illy-red);
            padding-bottom: 1rem;
            margin-bottom: 1.5rem;
        }

        .filter-card .uk-form-label {
            font-weight: 600;
            color: var(--illy-dark);
            margin-bottom: 0.5rem;
        }

        .filter-card .uk-input {
            border-radius: 6px;
            border: 2px solid #e0e0e0;
            padding: 10px 12px;
            transition: all 0.3s ease;
        }

        .filter-card .uk-input:focus {
            border-color: var(--illy-red);
            box-shadow: 0 0 0 3px rgba(217, 56, 30, 0.1);
        }

        /* Button Styling */
        .btn-filter {
            background: linear-gradient(135deg, var(--illy-red) 0%, #b82f18 100%);
            border: none;
            border-radius: 6px;
            padding: 10px 24px;
            font-weight: 600;
            color: white;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(217, 56, 30, 0.2);
        }

        .btn-filter:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(217, 56, 30, 0.3);
        }

        .btn-filter:active {
            transform: translateY(0);
        }

        .btn-export {
            background: white;
            border: 2px solid #e0e0e0;
            border-radius: 6px;
            padding: 10px 24px;
            font-weight: 600;
            color: var(--illy-dark);
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .btn-export:hover {
            border-color: var(--illy-red);
            color: var(--illy-red);
            background: rgba(217, 56, 30, 0.05);
        }

        /* Data Table */
        .data-table-container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
            overflow: hidden;
            border: 1px solid rgba(217, 56, 30, 0.1);
        }

        .uk-table {
            margin-bottom: 0;
        }

        .uk-table thead tr {
            background: linear-gradient(135deg, var(--illy-red) 0%, #b82f18 100%);
            color: white;
        }

        .uk-table thead th {
            font-weight: 700;
            padding: 1.25rem !important;
            text-transform: uppercase;
            font-size: 0.85rem;
            letter-spacing: 0.5px;
            border: none;
        }

        .uk-table tbody tr {
            transition: all 0.3s ease;
            border-bottom: 1px solid #f0f0f0;
        }

        .uk-table tbody tr:hover {
            background-color: rgba(217, 56, 30, 0.03);
            transform: scale(1.01);
        }

        .uk-table tbody td {
            padding: 1.25rem !important;
            vertical-align: middle;
            color: var(--illy-dark);
        }

        .table-empty {
            text-align: center;
            padding: 3rem !important;
            color: #999;
            font-size: 1.1rem;
        }

        /* Badge Styling */
        .score-badge {
            padding: 8px 14px;
            border-radius: 20px;
            font-weight: 700;
            font-size: 0.95rem;
            display: inline-block;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transition: all 0.3s ease;
        }

        .score-badge:hover {
            transform: scale(1.1);
        }

        .score-low {
            background: linear-gradient(135deg, var(--illy-red) 0%, #b82f18 100%);
            color: white;
        }

        .score-high {
            background: linear-gradient(135deg, #32d296 0%, #2ab881 100%);
            color: white;
        }

        .type-badge {
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 0.85rem;
            background: rgba(217, 56, 30, 0.15);
            color: var(--illy-red);
            display: inline-block;
        }

        /* Audio Icon */
        .audio-icon {
            color: var(--illy-red);
            font-size: 1.3rem;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .audio-icon:hover {
            transform: scale(1.2);
        }

        /* Conversation ID */
        .conversation-id {
            color: #999;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
            font-weight: 500;
        }

        /* Container */
        .uk-container {
            padding: 2rem !important;
        }

        /* Responsive */
        @media (max-width: 960px) {
            .illy-navbar {
                padding: 1rem !important;
            }

            .uk-container {
                padding: 1rem !important;
            }

            .filter-card {
                margin-bottom: 2rem;
            }
        }

        /* Animation */
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .filter-card, .data-table-container {
            animation: slideIn 0.5s ease-out;
        }

        .uk-table tbody tr {
            animation: slideIn 0.5s ease-out;
        }

        /* Loading State */
        .loading {
            opacity: 0.6;
            pointer-events: none;
        }
    </style>
</head>
<body>

    <!-- Navbar -->
    <nav class="uk-navbar-container illy-navbar" uk-navbar>
        <div class="uk-navbar-left">
            <a class="uk-navbar-item uk-logo uk-light" href="admin" style="color: white; text-decoration: none;">
                <span uk-icon="icon: settings; ratio: 1.2"></span> &nbsp; <span class="uk-hidden-small">illy Survey Admin</span>
            </a>
        </div>
        <div class="uk-navbar-right">
            <div class="uk-navbar-item uk-light">
                <span uk-icon="icon: user; ratio: 1.1"></span> &nbsp; <span class="uk-hidden-small">Admin</span>
            </div>
        </div>
    </nav>

    <!-- Main Container -->
    <div class="uk-container uk-margin-medium-top uk-margin-medium-bottom">
        
        <!-- Filter Section -->
        <div class="filter-card uk-card uk-card-body uk-margin-bottom">
            <h3 class="uk-card-title">
                <span uk-icon="icon: filter"></span> Filtri Ricerca
            </h3>
            
            <form action="admin" method="get">
                <div class="uk-grid-small uk-child-width-1-3@s uk-child-width-1-2@m" uk-grid>
                    <div>
                        <label class="uk-form-label" for="start">Dal (Data Inizio)</label>
                        <div class="uk-form-controls">
                            <input class="uk-input" id="start" type="date" name="startDate" value="${startDate}">
                        </div>
                    </div>
                    <div>
                        <label class="uk-form-label" for="end">Al (Data Fine)</label>
                        <div class="uk-form-controls">
                            <input class="uk-input" id="end" type="date" name="endDate" value="${endDate}">
                        </div>
                    </div>
                    <div uk-margin>
                        <button type="submit" class="btn-filter uk-margin-small-right">
                            <span uk-icon="icon: search; ratio: 0.9"></span> Filtra
                        </button>
                        <a href="admin?action=export&startDate=${startDate}&endDate=${endDate}" class="btn-export uk-margin-remove-vertical">
                            <span uk-icon="icon: download; ratio: 0.9"></span> Esporta CSV
                        </a>
                    </div>
                </div>
            </form>
        </div>

        <!-- Data Table Section -->
        <div class="data-table-container">
            <div class="uk-overflow-auto">
                <table class="uk-table uk-table-hover uk-table-middle">
                    <thead>
                        <tr>
                            <th class="uk-width-small">
                                <span uk-icon="icon: calendar"></span> Data
                            </th>
                            <th>
                                <span uk-icon="icon: link"></span> ID Interazione
                            </th>
                            <th>
                                <span uk-icon="icon: phone"></span> Telefono
                            </th>
                            <th>
                                <span uk-icon="icon: tag"></span> Tipo
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: star"></span> Voto
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: microphone"></span> Audio
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty reportList}">
                            <tr>
                                <td colspan="6" class="table-empty">
                                    <div uk-icon="icon: info; ratio: 2" style="color: #ddd; margin-bottom: 1rem;"></div>
                                    <div>Nessun dato trovato per i filtri selezionati.</div>
                                </td>
                            </tr>
                        </c:if>

                        <c:forEach var="item" items="${reportList}">
                            <tr>
                                <td>
                                    <strong>${item.date}</strong>
                                </td>
                                <td>
                                    <span class="conversation-id">${item.conversationId}</span>
                                </td>
                                <td>
                                    <strong>${item.customerPhone}</strong>
                                </td>
                                <td>
                                    <span class="type-badge">${item.type != null ? item.type : '-'}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.score < 6}">
                                            <span class="score-badge score-low">${item.score}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-badge score-high">${item.score}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${item.hasAudio == 'true'}">
                                        <span class="audio-icon uk-icon-button" uk-icon="microphone" title="Audio disponibile"></span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <script>
        // Add smooth scrolling
        document.querySelectorAll('a[href^="#"]').forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    target.scrollIntoView({ behavior: 'smooth' });
                }
            });
        });

        // Table row animation on load
        const rows = document.querySelectorAll('.uk-table tbody tr');
        rows.forEach((row, index) => {
            row.style.animation = `slideIn 0.5s ease-out ${index * 0.05}s both`;
        });
    </script>

</body>
</html>