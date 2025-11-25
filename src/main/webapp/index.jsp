<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

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
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/uikit@3.17.11/dist/js/uikit-icons.min.js"></script>
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        html {
            scroll-behavior: smooth;
        }

        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            min-height: 100vh;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            color: #333;
        }

        /* Illy Brand Colors */
        :root {
            --illy-red: #d9381e;
            --illy-dark: #1f1f1f;
            --illy-light: #f8f8f8;
            --illy-dark-red: #b82f18;
            --success-green: #32d296;
            --success-dark: #2ab881;
        }

        /* ============ Navbar ============ */
        .illy-navbar {
            background: #ffffff;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
            padding: 1.2rem 2rem !important;
            position: sticky;
            top: 0;
            z-index: 980;
            transition: all 0.3s ease;
            border-bottom: 3px solid var(--illy-red);
        }

        .illy-navbar:hover {
            box-shadow: 0 10px 40px rgba(217, 56, 30, 0.2);
        }

        .illy-navbar .uk-logo {
            font-weight: 700;
            font-size: 1.5rem;
            letter-spacing: -0.5px;
            display: flex;
            align-items: center;
            transition: all 0.3s ease;
            color: var(--illy-red) !important;
            text-shadow: none;
        }

        .illy-navbar .uk-logo:hover {
            transform: scale(1.05);
            color: var(--illy-dark-red) !important;
        }

        .admin-badge {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: white !important;
            padding: 0.75rem 1.5rem;
            border-radius: 12px;
            font-weight: 700;
            font-size: 1rem;
            border: 2px solid var(--illy-red);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 12px rgba(217, 56, 30, 0.2);
            cursor: pointer;
        }

        .admin-badge:hover {
            background: linear-gradient(135deg, var(--illy-dark-red) 0%, #9a2612 100%);
            border-color: #9a2612;
            box-shadow: 0 8px 24px rgba(217, 56, 30, 0.3);
        }

        .admin-badge:active {
            transform: translateY(0);
        }

        /* ============ Header Stats ============ */
        .stats-container {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            border-left: 4px solid var(--illy-red);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
        }

        .stat-card:hover {
            box-shadow: 0 12px 32px rgba(217, 56, 30, 0.2);
            border-left-color: var(--illy-dark-red);
        }

        .stat-card h4 {
            color: #999;
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 0.5rem;
            font-weight: 600;
        }

        .stat-card .stat-value {
            font-size: 2rem;
            font-weight: 700;
            color: var(--illy-red);
            margin-bottom: 0.5rem;
        }

        .stat-card .stat-change {
            font-size: 0.85rem;
            color: var(--success-green);
            font-weight: 600;
        }

        /* ============ Filter Card ============ */
        .filter-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
            border: 2px solid transparent;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            overflow: hidden;
            margin-bottom: 2rem;
        }

        .filter-card:hover {
            box-shadow: 0 16px 48px rgba(217, 56, 30, 0.15);
            border-color: rgba(217, 56, 30, 0.1);
        }

        .filter-header {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: #ffffff;
            padding: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.75rem;
            font-weight: 700;
            font-size: 1.1rem;
            letter-spacing: 0.5px;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
        }

        .filter-card .uk-card-body {
            padding: 2rem;
        }

        .filter-card .uk-form-label {
            font-weight: 600;
            color: var(--illy-dark);
            margin-bottom: 0.75rem;
            font-size: 0.95rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .filter-card .uk-input {
            border-radius: 8px;
            border: 2px solid #e8e8e8;
            padding: 12px 16px;
            transition: all 0.3s ease;
            font-size: 1rem;
            background: #fafafa;
        }

        .filter-card .uk-input:focus {
            border-color: var(--illy-red);
            background: white;
            box-shadow: 0 0 0 4px rgba(217, 56, 30, 0.1);
            outline: none;
        }

        /* ============ Button Styles ============ */
        .btn-filter {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            border: none;
            border-radius: 8px;
            padding: 12px 28px;
            font-weight: 700;
            color: white;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            box-shadow: 0 4px 16px rgba(217, 56, 30, 0.2);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-size: 0.9rem;
            position: relative;
            overflow: hidden;
        }

        .btn-filter::before {
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

        .btn-filter:hover::before {
            width: 300px;
            height: 300px;
        }

        .btn-filter:hover {
            transform: none;
            box-shadow: 0 8px 24px rgba(217, 56, 30, 0.3);
        }

        .btn-filter:active {
            transform: none;
            box-shadow: 0 4px 12px rgba(217, 56, 30, 0.2);
        }

        .btn-export {
            background: linear-gradient(135deg, #ffffff 0%, #f5f5f5 100%);
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            padding: 10px 26px;
            font-weight: 700;
            color: var(--illy-red);
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-size: 0.9rem;
        }

        .btn-export:hover {
            border-color: var(--illy-red);
            color: white;
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            box-shadow: 0 4px 16px rgba(217, 56, 30, 0.2);
        }

        /* ============ Data Table ============ */
        .data-table-container {
            background: white;
            border-radius: 16px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            border: 2px solid transparent;
            transition: all 0.3s ease;
            margin-bottom: 2rem;
        }

        .data-table-container:hover {
            box-shadow: 0 16px 48px rgba(217, 56, 30, 0.15);
            border-color: rgba(217, 56, 30, 0.1);
        }

        .uk-table {
            margin-bottom: 0;
        }

        .uk-table thead tr {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: white;
        }

        .uk-table thead th {
            font-weight: 700;
            padding: 1.5rem !important;
            text-transform: uppercase;
            font-size: 0.85rem;
            letter-spacing: 1px;
            border: none;
            vertical-align: middle;
            color: #ffffff !important;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
        }

        .uk-table tbody tr {
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            border-bottom: 1px solid #f0f0f0;
        }

        .uk-table tbody tr:hover {
            background-color: rgba(217, 56, 30, 0.08);
            box-shadow: inset 4px 0 0 var(--illy-red), 0 4px 12px rgba(217, 56, 30, 0.1);
        }

        .uk-table tbody tr:last-child {
            border-bottom: none;
        }

        .uk-table tbody td {
            padding: 1.5rem !important;
            vertical-align: middle;
            color: #1a1a1a;
            font-weight: 500;
        }

        .table-empty {
            text-align: center;
            padding: 4rem !important;
            color: #999;
            font-size: 1.1rem;
        }

        .table-empty .empty-icon {
            font-size: 3rem;
            color: #ddd;
            margin-bottom: 1rem;
            opacity: 0.6;
        }

        /* ============ Badge Styles ============ */
        .score-badge {
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 700;
            font-size: 0.95rem;
            display: inline-block;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            position: relative;
            overflow: hidden;
        }

        .score-badge:hover {
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
            letter-spacing: 0.5px;
        }

        .score-low {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: white;
        }

        .score-high {
            background: linear-gradient(135deg, var(--success-green) 0%, var(--success-dark) 100%);
            color: white;
        }

        .type-badge {
            padding: 6px 14px;
            border-radius: 8px;
            font-weight: 600;
            font-size: 0.85rem;
            background: rgba(217, 56, 30, 0.15);
            color: var(--illy-red);
            display: inline-block;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            transition: all 0.3s ease;
            cursor: pointer;
        }

        .type-badge:hover {
            background: rgba(217, 56, 30, 0.25);
            box-shadow: 0 4px 12px rgba(217, 56, 30, 0.15);
        }

        /* ============ Audio Icon ============ */
        .audio-icon {
            color: var(--illy-red);
            font-size: 1.3rem;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            border-radius: 8px;
            background: rgba(217, 56, 30, 0.1);
        }

        .audio-icon:hover {
            background: rgba(217, 56, 30, 0.2);
            box-shadow: 0 6px 16px rgba(217, 56, 30, 0.3);
        }

        /* ============ Conversation ID ============ */
        .conversation-id {
            color: #555;
            font-family: 'Courier New', 'Monaco', monospace;
            font-size: 0.9rem;
            font-weight: 600;
            background: rgba(0, 0, 0, 0.04);
            padding: 4px 8px;
            border-radius: 4px;
            letter-spacing: 0.5px;
            word-break: break-all;
        }

        /* ============ Container ============ */
        .uk-container {
            padding: 2.5rem !important;
            max-width: 1400px;
        }

        /* ============ Pagination ============ */
        .uk-pagination {
            margin-top: 2rem;
        }

        .uk-pagination a,
        .uk-pagination span {
            transition: all 0.3s ease;
            border-radius: 6px;
            font-weight: 600;
        }

        .uk-pagination a:hover {
            background-color: var(--illy-red);
            color: white;
        }

        .uk-pagination .uk-active span {
            background: var(--illy-red);
            color: white;
        }

        /* ============ Modal ============ */
        .uk-modal-dialog {
            border-radius: 16px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
        }

        .uk-modal-header {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: white;
            padding: 1.5rem;
            border: none;
        }

        .uk-modal-body {
            padding: 2rem;
        }

        /* ============ Tooltip ============ */
        [uk-tooltip] {
            text-decoration: none;
        }

        .uk-tooltip {
            border-radius: 8px !important;
            background: var(--illy-dark) !important;
            font-weight: 600;
            font-size: 0.85rem;
        }

        /* ============ Animations ============ */
        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
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

        @keyframes pulse {
            0%, 100% {
                opacity: 1;
            }
            50% {
                opacity: 0.7;
            }
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

        @keyframes scaleIn {
            from {
                opacity: 0;
                transform: scale(0.95);
            }
            to {
                opacity: 1;
                transform: scale(1);
            }
        }

        .filter-card {
            animation: slideInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) backwards;
        }

        .data-table-container {
            animation: slideInUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) 0.2s backwards;
        }

        .stats-container {
            animation: fadeIn 0.6s ease-out;
        }

        .stat-card {
            animation: scaleIn 0.5s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .stat-card:nth-child(1) { animation-delay: 0.1s; }
        .stat-card:nth-child(2) { animation-delay: 0.2s; }
        .stat-card:nth-child(3) { animation-delay: 0.3s; }
        .stat-card:nth-child(4) { animation-delay: 0.4s; }

        .uk-table tbody tr {
            animation: slideIn 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .uk-table tbody tr:nth-child(1) { animation-delay: 0s; }
        .uk-table tbody tr:nth-child(2) { animation-delay: 0.05s; }
        .uk-table tbody tr:nth-child(3) { animation-delay: 0.1s; }
        .uk-table tbody tr:nth-child(4) { animation-delay: 0.15s; }
        .uk-table tbody tr:nth-child(5) { animation-delay: 0.2s; }
        .uk-table tbody tr:nth-child(n+6) { animation-delay: 0.25s; }

        /* ============ Loading State ============ */
        .loading {
            opacity: 0.6;
            pointer-events: none;
        }

        .spinner {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(217, 56, 30, 0.3);
            border-top-color: var(--illy-red);
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        /* ============ Responsive Design ============ */
        @media (max-width: 1200px) {
            .uk-container {
                padding: 1.5rem !important;
            }

            .stats-container {
                grid-template-columns: repeat(2, 1fr);
            }
        }

        @media (max-width: 960px) {
            .illy-navbar {
                padding: 1rem !important;
            }

            .uk-container {
                padding: 1rem !important;
            }

            .stats-container {
                grid-template-columns: 1fr;
            }

            .filter-card {
                margin-bottom: 1.5rem;
            }

            .uk-table thead th,
            .uk-table tbody td {
                padding: 0.75rem !important;
                font-size: 0.9rem;
            }

            .uk-table thead th {
                text-transform: capitalize;
                font-size: 0.75rem;
            }

            .btn-filter, .btn-export {
                padding: 8px 16px;
                font-size: 0.8rem;
            }
        }

        @media (max-width: 640px) {
            .illy-navbar .uk-logo span {
                display: none;
            }

            .admin-badge span {
                display: none;
            }

            .uk-table {
                font-size: 0.9rem;
            }

            .uk-table thead th,
            .uk-table tbody td {
                padding: 0.5rem !important;
            }

            .score-badge {
                padding: 6px 12px;
                font-size: 0.85rem;
            }

            .type-badge {
                padding: 4px 8px;
                font-size: 0.75rem;
            }

            .conversation-id {
                font-size: 0.8rem;
            }

            .btn-filter, .btn-export {
                padding: 8px 12px;
                font-size: 0.75rem;
                white-space: nowrap;
            }

            .filter-card .uk-grid-small {
                display: flex;
                flex-direction: column;
            }

            .filter-card .uk-grid-small > div {
                width: 100% !important;
            }
        }

        /* ============ Scroll Bar ============ */
        ::-webkit-scrollbar {
            width: 12px;
            height: 12px;
        }

        ::-webkit-scrollbar-track {
            background: #f5f5f5;
        }

        ::-webkit-scrollbar-thumb {
            background: var(--illy-red);
            border-radius: 6px;
        }

        ::-webkit-scrollbar-thumb:hover {
            background: var(--illy-dark-red);
        }

        /* ============ Footer ============ */
        .illy-footer {
            text-align: center;
            padding: 2rem;
            color: #999;
            font-size: 0.9rem;
            border-top: 1px solid #e0e0e0;
            margin-top: 3rem;
        }

        .illy-footer a {
            color: var(--illy-red);
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
        }

        .illy-footer a:hover {
            text-decoration: underline;
            color: var(--illy-dark-red);
        }
    </style>
</head>
<body>

    <!-- Navbar -->
    <nav class="uk-navbar-container illy-navbar" uk-navbar>
        <div class="uk-navbar-left">
            <a class="uk-navbar-item uk-logo" href="admin" style="color: var(--illy-red); text-decoration: none;">
                <span uk-icon="icon: settings; ratio: 1.2" style="color: var(--illy-red);"></span> &nbsp; <span class="uk-hidden-small" style="color: var(--illy-red);">illy Survey Admin</span>
            </a>
        </div>
        <div class="uk-navbar-right">
            <div class="admin-badge">
                <span uk-icon="icon: user; ratio: 1.1" style="color: #ffffff;"></span> &nbsp; <span class="uk-hidden-small" style="color: #ffffff;">Amministratore</span>
            </div>
        </div>
    </nav>

    <!-- Main Container -->
    <div class="uk-container uk-margin-medium-top uk-margin-medium-bottom">
        
        <!-- Stats Section -->
        <div class="stats-container">
            <div class="stat-card" uk-tooltip="title: Numero totale di sondaggi raccolti; pos: top">
                <h4><span uk-icon="icon: check; ratio: 0.9"></span> Totale Sondaggi</h4>
                <div class="stat-value">${fn:length(reportList)}</div>
                <div class="stat-change">✓ Completati</div>
            </div>
            
            <div class="stat-card" uk-tooltip="title: Sondaggi con valutazione bassa; pos: top">
                <h4><span uk-icon="icon: close; ratio: 0.9"></span> Valutazioni Basse</h4>
                <div class="stat-value" id="lowScoreCount">0</div>
                <div class="stat-change">Voto &lt; 6</div>
            </div>
            
            <div class="stat-card" uk-tooltip="title: Sondaggi con valutazione alta; pos: top">
                <h4><span uk-icon="icon: check; ratio: 0.9"></span> Valutazioni Alte</h4>
                <div class="stat-value" id="highScoreCount">0</div>
                <div class="stat-change">Voto ≥ 6</div>
            </div>
            
            <div class="stat-card" uk-tooltip="title: Numero di audio disponibili; pos: top">
                <h4><span uk-icon="icon: microphone; ratio: 0.9"></span> Audio Disponibili</h4>
                <div class="stat-value" id="audioCount">0</div>
                <div class="stat-change">Registrazioni</div>
            </div>
        </div>
        
        <!-- Filter Section -->
        <div class="filter-card uk-card uk-card-body">
            <h3 class="filter-header">
                <span uk-icon="icon: filter" style="color: #ffffff;"></span> Filtri Ricerca Avanzata
            </h3>
            
            <form action="admin" method="get" class="uk-margin-top">
                <div class="uk-grid-small uk-child-width-1-3@s uk-child-width-1-2@m uk-child-width-auto@l" uk-grid>
                    <div>
                        <label class="uk-form-label" for="start">
                            <span uk-icon="icon: calendar; ratio: 0.9"></span> Dal (Data Inizio)
                        </label>
                        <div class="uk-form-controls">
                            <input class="uk-input" id="start" type="date" name="startDate" value="${startDate}" required>
                        </div>
                    </div>
                    <div>
                        <label class="uk-form-label" for="end">
                            <span uk-icon="icon: calendar; ratio: 0.9"></span> Al (Data Fine)
                        </label>
                        <div class="uk-form-controls">
                            <input class="uk-input" id="end" type="date" name="endDate" value="${endDate}" required>
                        </div>
                    </div>
                    <div>
                        <label class="uk-form-label">&nbsp;</label>
                        <div class="uk-form-controls uk-flex uk-flex-between uk-gap">
                            <button type="submit" class="btn-filter">
                                <span uk-icon="icon: search; ratio: 0.9"></span> Filtra
                            </button>
                            <a href="admin?action=export&startDate=${startDate}&endDate=${endDate}" class="btn-export" uk-tooltip="title: Scarica i dati in formato CSV">
                                <span uk-icon="icon: download; ratio: 0.9"></span> CSV
                            </a>
                        </div>
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
                                <span uk-icon="icon: calendar; ratio: 0.9" style="color: #ffffff;"></span> Data
                            </th>
                            <th>
                                <span uk-icon="icon: link; ratio: 0.9" style="color: #ffffff;"></span> ID Interazione
                            </th>
                            <th>
                                <span uk-icon="icon: phone; ratio: 0.9" style="color: #ffffff;"></span> Telefono
                            </th>
                            <th>
                                <span uk-icon="icon: tag; ratio: 0.9" style="color: #ffffff;"></span> Tipo
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: star; ratio: 0.9" style="color: #ffffff;"></span> Voto
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: microphone; ratio: 0.9" style="color: #ffffff;"></span> Audio
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty reportList}">
                            <tr>
                                <td colspan="6" class="table-empty">
                                    <div uk-icon="icon: info; ratio: 3" style="color: #ddd; margin-bottom: 1rem; opacity: 0.5;"></div>
                                    <div style="font-weight: 600; font-size: 1.1rem;">Nessun dato trovato</div>
                                    <div style="color: #bbb; margin-top: 0.5rem;">Nessun sondaggio per i filtri selezionati</div>
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
                                    <span class="type-badge" uk-tooltip="title: Tipo di sondaggio">${item.type != null ? item.type : '-'}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.score < 6}">
                                            <span class="score-badge score-low" uk-tooltip="title: Valutazione bassa (minore di 6)">${item.score}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-badge score-high" uk-tooltip="title: Valutazione buona (uguale o superiore a 6)">${item.score}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${item.hasAudio == 'true'}">
                                        <span class="audio-icon" uk-icon="microphone" uk-tooltip="title: Audio disponibile per questa interazione" onclick="openAudioModal('${item.conversationId}')"></span>
                                    </c:if>
                                    <c:if test="${item.hasAudio != 'true'}">
                                        <span style="color: #ddd; opacity: 0.5;">-</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <!-- Audio Modal -->
    <div id="audio-modal" uk-modal>
        <div class="uk-modal-dialog uk-modal-body">
            <button class="uk-modal-close-default" type="button" uk-close></button>
            <h2 class="uk-modal-title" style="color: var(--illy-red);">
                <span uk-icon="icon: microphone; ratio: 1.2"></span> Riproduttore Audio
            </h2>
            <div class="uk-margin-top uk-margin-bottom">
                <p style="color: #666; margin-bottom: 1rem; font-weight: 600;">
                    <span uk-icon="icon: link; ratio: 0.9"></span> ID Interazione: <strong id="audio-modal-id" style="color: var(--illy-red);">-</strong>
                </p>
                <audio id="audio-player" controls style="width: 100%; border-radius: 8px; outline: none;">
                    Il tuo browser non supporta la riproduzione audio.
                </audio>
                <div style="margin-top: 1rem; padding: 1rem; background: rgba(217, 56, 30, 0.05); border-radius: 8px; border-left: 4px solid var(--illy-red);">
                    <p style="margin: 0; font-size: 0.9rem; color: #666;">
                        <span uk-icon="icon: info; ratio: 0.8"></span> 
                        Utilizza i controlli riproduzione per ascoltare la registrazione audio della conversazione.
                    </p>
                </div>
            </div>
            <div class="uk-text-right uk-margin-top">
                <button class="uk-button uk-button-default uk-modal-close">Chiudi</button>
            </div>
        </div>
    </div>

    <!-- Details Modal -->
    <div id="details-modal" uk-modal>
        <div class="uk-modal-dialog uk-modal-body uk-form-stacked">
            <button class="uk-modal-close-default" type="button" uk-close></button>
            <h2 class="uk-modal-title" style="color: var(--illy-red);">
                <span uk-icon="icon: info; ratio: 1.2"></span> Dettagli Sondaggio
            </h2>
            <div id="details-content" class="uk-margin-top">
                <!-- Contenuto caricato dinamicamente -->
            </div>
            <div class="uk-text-right">
                <button class="uk-button uk-button-default uk-modal-close">Chiudi</button>
            </div>
        </div>
    </div>

    <footer class="illy-footer">
        <div>© 2025 Illy Survey Admin - Tutti i diritti riservati</div>
        <div style="margin-top: 0.5rem; font-size: 0.85rem; color: #bbb;">
            <span uk-icon="icon: shield; ratio: 0.8"></span> Sistema Sicuro e Protetto
        </div>
    </footer>

    <script>
        // Calculate statistics
        function calculateStats() {
            const lowScores = document.querySelectorAll('.score-low').length;
            const highScores = document.querySelectorAll('.score-high').length;
            const audioItems = document.querySelectorAll('.audio-icon').length;
            
            document.getElementById('lowScoreCount').textContent = lowScores;
            document.getElementById('highScoreCount').textContent = highScores;
            document.getElementById('audioCount').textContent = audioItems;
        }

        // Open audio modal
        function openAudioModal(conversationId) {
            document.getElementById('audio-modal-id').textContent = conversationId;
            // In a real scenario, you would fetch the audio file here
            document.getElementById('audio-player').src = 'api/audio/' + conversationId;
            UIkit.modal(document.getElementById('audio-modal')).show();
        }

        // Initialize on page load
        document.addEventListener('DOMContentLoaded', function() {
            calculateStats();
            
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

            // Add ripple effect to buttons
            document.querySelectorAll('.btn-filter, .btn-export').forEach(button => {
                button.addEventListener('click', function(e) {
                    const rect = this.getBoundingClientRect();
                    const ripple = document.createElement('span');
                    ripple.style.position = 'absolute';
                    ripple.style.borderRadius = '50%';
                    ripple.style.background = 'rgba(255, 255, 255, 0.5)';
                    ripple.style.width = '20px';
                    ripple.style.height = '20px';
                    ripple.style.left = (e.clientX - rect.left - 10) + 'px';
                    ripple.style.top = (e.clientY - rect.top - 10) + 'px';
                    ripple.style.animation = 'ripple-animation 0.6s ease-out';
                    this.appendChild(ripple);
                    
                    setTimeout(() => ripple.remove(), 600);
                });
            });
        });

        // Ripple animation
        const style = document.createElement('style');
        style.textContent = `
            @keyframes ripple-animation {
                from {
                    width: 20px;
                    height: 20px;
                    opacity: 1;
                    transform: scale(1);
                }
                to {
                    width: 200px;
                    height: 200px;
                    opacity: 0;
                    transform: scale(10);
                }
            }
        `;
        document.head.appendChild(style);
    </script>

</body>
</html>