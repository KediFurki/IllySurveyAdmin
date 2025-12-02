<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>

<% 
    // Prevent caching to avoid back button issues after logout
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
    
    if (request.getAttribute("reportList") == null) {
        response.sendRedirect("admin");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Illy - Survey Reports</title>
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

        .user-info-container {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
        }

        .user-name {
            font-size: 1rem;
            font-weight: 700;
        }

        .user-email {
            font-size: 0.75rem;
            opacity: 0.9;
            font-weight: 500;
        }

        /* ============ Logout Button ============ */
        .logout-btn {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            background: linear-gradient(135deg, #6c757d 0%, #495057 100%);
            color: white !important;
            padding: 0.6rem 1.2rem;
            border-radius: 10px;
            font-weight: 600;
            font-size: 0.9rem;
            border: 2px solid #6c757d;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            cursor: pointer;
            text-decoration: none;
            margin-left: 1rem;
            box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
        }

        .logout-btn:hover {
            background: linear-gradient(135deg, #5a6268 0%, #343a40 100%);
            border-color: #495057;
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(108, 117, 125, 0.4);
            color: white !important;
        }

        .logout-btn:active {
            transform: translateY(0);
            box-shadow: 0 2px 8px rgba(108, 117, 125, 0.3);
        }

        .user-section {
            display: flex;
            align-items: center;
            gap: 0.5rem;
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
            cursor: pointer;
            user-select: none;
            transition: all 0.3s ease;
        }

        .filter-header:hover {
            background: linear-gradient(135deg, var(--illy-dark-red) 0%, #9a2612 100%);
        }

        .filter-toggle-icon {
            margin-left: auto;
            transition: transform 0.3s ease;
        }

        .filter-toggle-icon.closed {
            transform: rotate(180deg);
        }

        .filter-card .uk-card-body {
            padding: 2rem;
        }

        .filter-card .uk-form-label {
            font-weight: 600;
            color: var(--illy-dark);
            margin-bottom: 0.75rem;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            height: auto;
            line-height: 1.4;
        }

        .filter-card .uk-form-controls {
            margin-top: 0.5rem;
            margin-bottom: 0;
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

        .filter-card .uk-select {
            border-radius: 8px;
            border: 2px solid #e8e8e8;
            padding: 12px 16px;
            transition: all 0.3s ease;
            font-size: 1rem;
            background: #fafafa;
            color: #333;
            height: auto;
            min-height: 44px;
            line-height: 1.5;
            position: relative;
            z-index: 100;
        }

        .filter-card .uk-select:focus {
            border-color: var(--illy-red);
            background: white;
            box-shadow: 0 0 0 4px rgba(217, 56, 30, 0.1);
            outline: none;
        }

        .filter-card .uk-select option {
            padding: 12px 16px;
            line-height: 1.6;
            background: white;
            color: #333;
            font-size: 1rem;
            min-height: 40px;
        }

        .filter-card .uk-select option:hover {
            background: rgba(217, 56, 30, 0.1);
            color: #d9381e;
        }

        .filter-card .uk-select option:checked {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            color: white;
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

        .btn-reset {
            background: linear-gradient(135deg, #6c757d 0%, #5a6268 100%);
            border: 2px solid #6c757d;
            border-radius: 8px;
            padding: 10px 20px;
            font-weight: 700;
            color: white;
            cursor: pointer;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            font-size: 0.9rem;
            position: relative;
            overflow: hidden;
        }

        .btn-reset::before {
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

        .btn-reset:hover::before {
            width: 300px;
            height: 300px;
        }

        .btn-reset:hover {
            transform: none;
            box-shadow: 0 8px 24px rgba(108, 117, 125, 0.3);
            background: linear-gradient(135deg, #5a6268 0%, #495057 100%);
        }

        .btn-reset:active {
            transform: none;
            box-shadow: 0 4px 12px rgba(108, 117, 125, 0.2);
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

        /* ============ Date Badge ============ */
        .date-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            background: linear-gradient(135deg, rgba(217, 56, 30, 0.1) 0%, rgba(217, 56, 30, 0.05) 100%);
            color: #d9381e;
            padding: 0.6rem 1rem;
            border-radius: 8px;
            font-weight: 600;
            font-size: 0.95rem;
            border: 1px solid rgba(217, 56, 30, 0.2);
            transition: all 0.3s ease;
            white-space: nowrap;
        }

        .date-badge:hover {
            background: linear-gradient(135deg, rgba(217, 56, 30, 0.15) 0%, rgba(217, 56, 30, 0.1) 100%);
            border-color: rgba(217, 56, 30, 0.4);
            box-shadow: 0 2px 8px rgba(217, 56, 30, 0.1);
        }

        .date-badge-icon {
            font-size: 1.1rem;
        }

        .date-badge-text {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
        }

        .date-badge-label {
            font-size: 0.75rem;
            opacity: 0.8;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .date-badge-value {
            font-size: 0.95rem;
            font-weight: 700;
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

            .admin-badge .user-info-container {
                display: none !important;
            }

            .admin-badge {
                padding: 0.5rem;
            }

            .logout-btn {
                padding: 0.5rem;
                margin-left: 0.5rem;
            }

            .logout-btn span.uk-hidden-small {
                display: none !important;
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

        /* ============ Custom Audio Player ============ */
        .audio-player-container {
            background: linear-gradient(135deg, var(--illy-red) 0%, var(--illy-dark-red) 100%);
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 8px 24px rgba(217, 56, 30, 0.25);
            margin-top: 1.5rem;
            margin-bottom: 1.5rem;
        }

        .audio-player {
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
        }

        .audio-controls {
            display: flex;
            align-items: center;
            gap: 1rem;
            flex-wrap: wrap;
            justify-content: center;
        }

        .audio-button-group {
            display: flex;
            align-items: center;
            gap: 0.8rem;
        }

        .audio-play-btn, .audio-pause-btn {
            background: rgba(255, 255, 255, 0.25);
            border: 2px solid rgba(255, 255, 255, 0.5);
            color: white;
            width: 56px;
            height: 56px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            font-size: 1.4rem;
            font-weight: bold;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .audio-play-btn:hover, .audio-pause-btn:hover {
            background: rgba(255, 255, 255, 0.35);
            border-color: white;
            transform: scale(1.1);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
        }

        .audio-play-btn:active, .audio-pause-btn:active {
            transform: scale(0.95);
        }

        .audio-skip-button {
            background: rgba(255, 255, 255, 0.2);
            border: 2px solid rgba(255, 255, 255, 0.4);
            color: white;
            width: 44px;
            height: 44px;
            border-radius: 8px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
            font-size: 0.95rem;
            font-weight: bold;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .audio-skip-button:hover {
            background: rgba(255, 255, 255, 0.3);
            border-color: rgba(255, 255, 255, 0.7);
            transform: scale(1.05);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .audio-skip-button:active {
            transform: scale(0.95);
        }

        .audio-progress-section {
            width: 100%;
            display: flex;
            flex-direction: column;
            gap: 0.8rem;
        }

        .audio-progress-bar {
            width: 100%;
            height: 8px;
            background: rgba(255, 255, 255, 0.25);
            border-radius: 4px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .audio-progress-fill {
            height: 100%;
            background: rgba(255, 255, 255, 0.9);
            border-radius: 4px;
            width: 0%;
            transition: width 0.1s linear;
            box-shadow: 0 0 6px rgba(255, 255, 255, 0.5);
        }

        .audio-progress-bar:hover .audio-progress-fill {
            background: white;
            box-shadow: 0 0 8px rgba(255, 255, 255, 0.7);
        }

        .audio-time-display {
            display: flex;
            justify-content: space-between;
            align-items: center;
            color: rgba(255, 255, 255, 0.9);
            font-size: 0.9rem;
            font-weight: 600;
            font-family: 'Courier New', monospace;
        }

        .audio-volume-section {
            display: flex;
            align-items: center;
            gap: 0.8rem;
            min-width: 200px;
            justify-content: center;
        }

        .audio-volume-icon {
            color: white;
            font-size: 1.2rem;
            min-width: 24px;
            text-align: center;
        }

        .audio-volume-slider {
            width: 120px;
            height: 5px;
            appearance: none;
            background: rgba(255, 255, 255, 0.25);
            border-radius: 3px;
            outline: none;
            cursor: pointer;
            transition: background 0.3s ease;
        }

        .audio-volume-slider:hover {
            background: rgba(255, 255, 255, 0.35);
        }

        .audio-volume-slider::-webkit-slider-thumb {
            appearance: none;
            width: 16px;
            height: 16px;
            background: white;
            border-radius: 50%;
            cursor: pointer;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
            transition: all 0.2s ease;
        }

        .audio-volume-slider::-webkit-slider-thumb:hover {
            transform: scale(1.2);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
        }

        .audio-volume-slider::-moz-range-thumb {
            width: 16px;
            height: 16px;
            background: white;
            border: none;
            border-radius: 50%;
            cursor: pointer;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
            transition: all 0.2s ease;
        }

        .audio-volume-slider::-moz-range-thumb:hover {
            transform: scale(1.2);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
        }

        .audio-volume-value {
            color: rgba(255, 255, 255, 0.8);
            font-size: 0.85rem;
            font-weight: 600;
            min-width: 30px;
            text-align: right;
        }

        @media (max-width: 768px) {
            .audio-controls {
                gap: 0.8rem;
            }

            .audio-play-btn, .audio-pause-btn {
                width: 48px;
                height: 48px;
                font-size: 1.2rem;
            }

            .audio-skip-button {
                width: 40px;
                height: 40px;
                font-size: 0.85rem;
            }

            .audio-player-container {
                padding: 1rem;
            }

            .audio-volume-section {
                min-width: 150px;
                gap: 0.5rem;
            }

            .audio-volume-slider {
                width: 80px;
            }
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
            <div class="user-section">
                <div class="admin-badge" id="userBadge">
                    <span uk-icon="icon: user; ratio: 1.1" style="color: #ffffff;"></span>
                    <div class="user-info-container uk-hidden-small">
                        <div class="user-name" id="userName">
                            <%= session.getAttribute("userName") != null ? session.getAttribute("userName") : "Administrator" %>
                        </div>
                        <div class="user-email" id="userEmail">
                            <%= session.getAttribute("userEmail") != null ? session.getAttribute("userEmail") : "" %>
                        </div>
                    </div>
                </div>
                <a href="logout" class="logout-btn" title="Exit the system">
                    <span uk-icon="icon: sign-out; ratio: 1"></span>
                    <span class="uk-hidden-small">Logout</span>
                </a>
            </div>
        </div>
    </nav>

    <!-- Main Container -->
    <div class="uk-container uk-margin-medium-top uk-margin-medium-bottom">
        
        <!-- Stats Section -->
        <div class="stats-container">
            <div class="stat-card" uk-tooltip="title: Total number of surveys collected; pos: top">
                <h4><span uk-icon="icon: check; ratio: 0.9"></span> Total Surveys</h4>
                <div class="stat-value">${fn:length(reportList)}</div>
                <div class="stat-change">✓ Completed</div>
            </div>
            
            <div class="stat-card" uk-tooltip="title: Surveys with low rating; pos: top">
                <h4><span uk-icon="icon: close; ratio: 0.9"></span> Low Ratings</h4>
                <div class="stat-value" id="lowScoreCount">0</div>
                <div class="stat-change">Score &lt; 6</div>
            </div>
            
            <div class="stat-card" uk-tooltip="title: Surveys with high rating; pos: top">
                <h4><span uk-icon="icon: check; ratio: 0.9"></span> High Ratings</h4>
                <div class="stat-value" id="highScoreCount">0</div>
                <div class="stat-change">Score ≥ 6</div>
            </div>
            
            <div class="stat-card" uk-tooltip="title: Number of available audio files; pos: top">
                <h4><span uk-icon="icon: microphone; ratio: 0.9"></span> Available Audio</h4>
                <div class="stat-value" id="audioCount">0</div>
                <div class="stat-change">Recordings</div>
            </div>
        </div>
        
        <!-- Filter Section -->
        <div class="filter-card uk-card uk-card-body">
            <h3 class="filter-header" onclick="toggleFilterPanel()" style="margin: 0; padding: 1.5rem; cursor: pointer; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1rem;">
                <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
                    <span uk-icon="icon: filter" style="color: #ffffff;"></span> 
                    <span>Advanced Search Filters</span>
                    <span id="filterBadge" style="background: rgba(255, 255, 255, 0.3); padding: 0.3rem 0.8rem; border-radius: 12px; font-size: 0.85rem; font-weight: 600; display: none; white-space: nowrap;"></span>
                </div>
                <span class="filter-toggle-icon" id="filterToggleIcon" uk-icon="icon: chevron-down; ratio: 1.2" style="color: #ffffff;"></span>
            </h3>
            
            <div id="filterPanel" style="display: none; padding: 2rem; border-top: 2px solid #f0f0f0;">
                <form action="admin" method="get" class="uk-margin-top" id="filterForm">
                    <div class="uk-grid-small uk-child-width-1-2@s uk-child-width-1-4@m" uk-grid>
                        <div>
                            <label class="uk-form-label" for="start">
                                <span uk-icon="icon: calendar; ratio: 0.9"></span> From
                            </label>
                            <div class="uk-form-controls">
                                <input class="uk-input" id="start" type="date" name="startDate" value="${startDate}" required>
                            </div>
                        </div>
                        <div>
                            <label class="uk-form-label" for="end">
                                <span uk-icon="icon: calendar; ratio: 0.9"></span> To
                            </label>
                            <div class="uk-form-controls">
                                <input class="uk-input" id="end" type="date" name="endDate" value="${endDate}" required>
                            </div>
                        </div>
                        <div>
                            <label class="uk-form-label" for="conversationId">
                                <span uk-icon="icon: link; ratio: 0.9"></span> Conv. ID
                            </label>
                            <div class="uk-form-controls">
                                <input class="uk-input" id="conversationId" type="text" name="conversationId" placeholder="ID..." value="${param.conversationId}">
                            </div>
                        </div>
                        <div>
                            <label class="uk-form-label" for="phone">
                                <span uk-icon="icon: phone; ratio: 0.9"></span> Phone
                            </label>
                            <div class="uk-form-controls">
                                <input class="uk-input" id="phone" type="text" name="phone" placeholder="+39..." value="${param.phone}">
                            </div>
                        </div>
                    </div>
                    
                    <div class="uk-margin-top uk-grid-small uk-child-width-1-2@s uk-child-width-1-4@m" uk-grid>
                        <div>
                            <label class="uk-form-label" for="type">
                                <span uk-icon="icon: tag; ratio: 0.9"></span> Category
                            </label>
                            <div class="uk-form-controls">
                                <select class="uk-select" id="type" name="type">
                                    <option value="">All</option>
                                    <option value="B2B" ${param.type == 'B2B' ? 'selected' : ''}>B2B</option>
                                    <option value="B2C" ${param.type == 'B2C' ? 'selected' : ''}>B2C</option>
                                </select>
                            </div>
                        </div>
                        <div>
                            <label class="uk-form-label" for="score">
                                <span uk-icon="icon: star; ratio: 0.9"></span> Rating
                            </label>
                            <div class="uk-form-controls">
                                <select class="uk-select" id="score" name="score">
                                    <option value="">All</option>
                                    <option value="0-5" ${param.score == '0-5' ? 'selected' : ''}>Low</option>
                                    <option value="6-10" ${param.score == '6-10' ? 'selected' : ''}>High</option>
                                    <option value="none" ${param.score == 'none' ? 'selected' : ''}>None</option>
                                </select>
                            </div>
                        </div>
                        <div>
                            <label class="uk-form-label" for="audio">
                                <span uk-icon="icon: microphone; ratio: 0.9"></span> Audio
                            </label>
                            <div class="uk-form-controls">
                                <select class="uk-select" id="audio" name="audio">
                                    <option value="">All</option>
                                    <option value="yes" ${param.audio == 'yes' ? 'selected' : ''}>With</option>
                                    <option value="no" ${param.audio == 'no' ? 'selected' : ''}>Without</option>
                                </select>
                            </div>
                        </div>
                        <div>
                            <label class="uk-form-label">&nbsp;</label>
                            <div class="uk-form-controls uk-flex uk-gap" style="gap: 0.5rem;">
                                <button type="submit" class="btn-filter" style="flex: 1;" title="Apply filters">
                                    <span uk-icon="icon: search; ratio: 0.9"></span>
                                </button>
                                <button type="button" class="btn-reset" onclick="resetFilters()" title="Reset filters">
                                    <span uk-icon="icon: refresh; ratio: 0.9"></span>
                                </button>
                                <a href="admin?action=export&startDate=${startDate}&endDate=${endDate}&type=${param.type}&score=${param.score}&audio=${param.audio}" class="btn-export" target="_blank" uk-tooltip="title: Download CSV" style="flex: 1; text-align: center; text-decoration: none; display: flex; align-items: center; justify-content: center;">
                                    <span uk-icon="icon: download; ratio: 0.9"></span>
                                </a>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Data Table Section -->
        <div class="data-table-container">
            <div class="uk-overflow-auto">
                <table class="uk-table uk-table-hover uk-table-middle">
                    <thead>
                        <tr>
                            <th class="uk-width-small">
                                <span uk-icon="icon: calendar; ratio: 0.9" style="color: #ffffff;"></span> Date
                            </th>
                            <th>
                                <span uk-icon="icon: link; ratio: 0.9" style="color: #ffffff;"></span> Interaction ID
                            </th>
                            <th>
                                <span uk-icon="icon: phone; ratio: 0.9" style="color: #ffffff;"></span> Phone
                            </th>
                            <th>
                                <span uk-icon="icon: tag; ratio: 0.9" style="color: #ffffff;"></span> Type
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: star; ratio: 0.9" style="color: #ffffff;"></span> Service<br><span class="uk-text-small" style="opacity: 0.85; font-weight: 500;">(0-5)</span>
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: star; ratio: 0.9" style="color: #ffffff;"></span> NPS<br><span class="uk-text-small" style="opacity: 0.85; font-weight: 500;">(0-10)</span>
                            </th>
                            <th class="uk-width-small">
                                <span uk-icon="icon: microphone; ratio: 0.9" style="color: #ffffff;"></span> Audio
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty reportList}">
                            <tr>
                                <td colspan="7" class="table-empty">
                                    <div uk-icon="icon: info; ratio: 3" style="color: #ddd; margin-bottom: 1rem; opacity: 0.5;"></div>
                                    <div style="font-weight: 600; font-size: 1.1rem;">No data found</div>
                                    <div style="color: #bbb; margin-top: 0.5rem;">No surveys for the selected filters</div>
                                </td>
                            </tr>
                        </c:if>

                        <c:forEach var="item" items="${reportList}">
                            <tr>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty item.date}">
                                            <div class="date-badge">
                                                <span class="date-badge-icon" uk-icon="icon: calendar; ratio: 1"></span>
                                                <span class="date-badge-text">
                                                    <span class="date-badge-label">Date</span>
                                                    <span class="date-badge-value" data-raw-date="${item.date}">${fn:substring(item.date, 0, 10)} ${fn:substring(item.date, 11, 16)}</span>
                                                </span>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: #999;">-</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="conversation-id">${item.conversationId}</span>
                                </td>
                                <td>
                                    <strong>${item.customerPhone}</strong>
                                </td>
                                <td>
                                    <span class="type-badge" uk-tooltip="title: Survey type">${item.type != null ? item.type : '-'}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.scoreQ1 == null || item.scoreQ1 == ''}">
                                            <span class="score-badge" style="background: #f0f0f0; color: #999;" uk-tooltip="title: No score">-</span>
                                        </c:when>
                                        <c:when test="${item.scoreQ1 < 3}">
                                            <span class="score-badge score-low" uk-tooltip="title: Low rating (less than 3)">${item.scoreQ1}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-badge score-high" uk-tooltip="title: Good rating (3 or higher)">${item.scoreQ1}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.scoreQ2 == null || item.scoreQ2 == ''}">
                                            <span class="score-badge" style="background: #f0f0f0; color: #999;" uk-tooltip="title: No score">-</span>
                                        </c:when>
                                        <c:when test="${item.scoreQ2 < 6}">
                                            <span class="score-badge score-low" uk-tooltip="title: Low rating (less than 6)">${item.scoreQ2}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-badge score-high" uk-tooltip="title: Good rating (6 or higher)">${item.scoreQ2}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${item.hasAudio == 'true'}">
                                        <span class="audio-icon" uk-icon="microphone" uk-tooltip="title: Audio available for this conversation" onclick="openAudioModal('${item.conversationId}')"></span>
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
                <span uk-icon="icon: microphone; ratio: 1.2"></span> Audio Player
            </h2>
            
            <div class="uk-margin-top">
                <p style="color: #666; margin-bottom: 1rem; font-weight: 600;">
                    <span uk-icon="icon: link; ratio: 0.9"></span> Interaction ID: <strong id="audio-modal-id" style="color: var(--illy-red);">-</strong>
                </p>
            </div>

            <div class="audio-player-container">
                <div class="audio-player">
                    <!-- Main Controls -->
                    <div class="audio-controls">
                        <div class="audio-button-group">
                            <!-- Play Button -->
                            <button class="audio-play-btn" id="playBtn" onclick="playAudio()" style="display: flex;" title="Play">
                                ▶
                            </button>
                            
                            <!-- Pause Button -->
                            <button class="audio-pause-btn" id="pauseBtn" onclick="pauseAudio()" style="display: none;" title="Pause">
                                ⏸
                            </button>

                            <!-- Skip Buttons -->
                            <button class="audio-skip-button" id="rewindBtn" onclick="rewindAudio()" title="Rewind 5 seconds">
                                -5s
                            </button>
                            <button class="audio-skip-button" id="forwardBtn" onclick="forwardAudio()" title="Forward 5 seconds">
                                +5s
                            </button>
                        </div>
                    </div>

                    <!-- Progress Bar Section -->
                    <div class="audio-progress-section">
                        <div class="audio-progress-bar" id="audioProgressBar" onclick="seekAudio(event)">
                            <div class="audio-progress-fill" id="audioProgressFill"></div>
                        </div>
                        <div class="audio-time-display">
                            <span id="audioCurrentTime">0:00</span>
                            <span id="audioDuration">0:00</span>
                        </div>
                    </div>

                    <!-- Volume Control Section -->
                    <div class="audio-volume-section">
                        <div class="audio-volume-icon">
                            <span uk-icon="icon: volume-high; ratio: 1"></span>
                        </div>
                        <input type="range" class="audio-volume-slider" id="volumeSlider" min="0" max="100" value="100" onchange="setVolume(this.value)" title="Adjust volume">
                        <div class="audio-volume-value" id="volumeValue">100%</div>
                    </div>
                </div>
            </div>

            <!-- Hidden audio element -->
            <audio id="audio-player" style="display: none;">
                Your browser does not support audio playback.
            </audio>

            <div style="margin-top: 1rem; padding: 1rem; background: rgba(217, 56, 30, 0.05); border-radius: 8px; border-left: 4px solid var(--illy-red);">
                <p style="margin: 0; font-size: 0.9rem; color: #666;">
                    <span uk-icon="icon: info; ratio: 0.8"></span> 
                    Play and control the recording with the buttons. Use +5s and -5s to skip quickly.
                </p>
            </div>

            <div class="uk-text-right uk-margin-top">
                <button class="uk-button uk-button-default uk-modal-close" onclick="stopAudio()">Close</button>
            </div>
        </div>
    </div>

    <!-- Details Modal -->
    <div id="details-modal" uk-modal>
        <div class="uk-modal-dialog uk-modal-body uk-form-stacked">
            <button class="uk-modal-close-default" type="button" uk-close></button>
            <h2 class="uk-modal-title" style="color: var(--illy-red);">
                <span uk-icon="icon: info; ratio: 1.2"></span> Survey Details
            </h2>
            <div id="details-content" class="uk-margin-top">
                <!-- Content loaded dynamically -->
            </div>
            <div class="uk-text-right">
                <button class="uk-button uk-button-default uk-modal-close">Close</button>
            </div>
        </div>
    </div>

    <footer class="illy-footer">
        <div>© 2025 Illy Survey Admin - All rights reserved</div>
        <div style="margin-top: 0.5rem; font-size: 0.85rem; color: #bbb;">
            <span uk-icon="icon: shield; ratio: 0.8"></span> Secure and Protected System
        </div>
    </footer>

    <script>
        // ============ Filter Panel Toggle ============
        function toggleFilterPanel() {
            const filterPanel = document.getElementById('filterPanel');
            const toggleIcon = document.getElementById('filterToggleIcon');
            
            if (filterPanel.style.display === 'none') {
                filterPanel.style.display = 'block';
                toggleIcon.classList.remove('closed');
            } else {
                filterPanel.style.display = 'none';
                toggleIcon.classList.add('closed');
            }
        }

        // Reset all filters
        function resetFilters() {
            // Reset all inputs and selects
            document.getElementById('conversationId').value = '';
            document.getElementById('phone').value = '';
            document.getElementById('type').value = '';
            document.getElementById('score').value = '';
            document.getElementById('audio').value = '';
            
            // Update badge
            updateFilterBadge();
            
            // Redirect to main page (without filters)
            window.location.href = 'admin';
        }

        // Update filter badge
        function updateFilterBadge() {
            const badge = document.getElementById('filterBadge');
            const conversationId = document.getElementById('conversationId').value;
            const phone = document.getElementById('phone').value;
            const type = document.getElementById('type').value;
            const score = document.getElementById('score').value;
            const audio = document.getElementById('audio').value;
            
            const activeFilters = [];
            if (conversationId) activeFilters.push(conversationId);
            if (phone) activeFilters.push(phone);
            if (type) activeFilters.push(type);
            if (score) activeFilters.push(score === '0-5' ? 'Low' : score === '6-10' ? 'High' : 'No score');
            if (audio) activeFilters.push(audio === 'yes' ? 'With Audio' : 'Without Audio');
            
            if (activeFilters.length > 0) {
                badge.textContent = `${activeFilters.length} filter${activeFilters.length > 1 ? 's' : ''} active`;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }

        // ============ Audio Player Functions ============
        const audioPlayer = document.getElementById('audio-player');
        const playBtn = document.getElementById('playBtn');
        const pauseBtn = document.getElementById('pauseBtn');
        const audioProgressBar = document.getElementById('audioProgressBar');
        const audioProgressFill = document.getElementById('audioProgressFill');
        const audioCurrentTime = document.getElementById('audioCurrentTime');
        const audioDuration = document.getElementById('audioDuration');
        const volumeSlider = document.getElementById('volumeSlider');
        const volumeValue = document.getElementById('volumeValue');

        function formatTime(seconds) {
            if (isNaN(seconds) || seconds === Infinity) return '0:00';
            const mins = Math.floor(seconds / 60);
            const secs = Math.floor(seconds % 60);
            return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
        }

        function updateProgress() {
            if (audioPlayer.duration) {
                const percent = (audioPlayer.currentTime / audioPlayer.duration) * 100;
                audioProgressFill.style.width = percent + '%';
                audioCurrentTime.textContent = formatTime(audioPlayer.currentTime);
                audioDuration.textContent = formatTime(audioPlayer.duration);
            }
        }

        function playAudio() {
            if (audioPlayer.src) {
                audioPlayer.play();
                playBtn.style.display = 'none';
                pauseBtn.style.display = 'flex';
            }
        }

        function pauseAudio() {
            audioPlayer.pause();
            pauseBtn.style.display = 'none';
            playBtn.style.display = 'flex';
        }

        function stopAudio() {
            audioPlayer.pause();
            audioPlayer.currentTime = 0;
            pauseBtn.style.display = 'none';
            playBtn.style.display = 'flex';
            audioProgressFill.style.width = '0%';
            audioCurrentTime.textContent = '0:00';
        }

        function seekAudio(event) {
            if (audioPlayer.duration) {
                const rect = audioProgressBar.getBoundingClientRect();
                const percent = (event.clientX - rect.left) / rect.width;
                audioPlayer.currentTime = Math.max(0, Math.min(percent * audioPlayer.duration, audioPlayer.duration));
            }
        }

        function rewindAudio() {
            audioPlayer.currentTime = Math.max(0, audioPlayer.currentTime - 5);
        }

        function forwardAudio() {
            audioPlayer.currentTime = Math.min(audioPlayer.duration, audioPlayer.currentTime + 5);
        }

        function setVolume(value) {
            audioPlayer.volume = value / 100;
            volumeValue.textContent = value + '%';
        }

        // Update progress when audio plays
        audioPlayer.addEventListener('timeupdate', updateProgress);

        // Handle audio end
        audioPlayer.addEventListener('ended', function() {
            pauseBtn.style.display = 'none';
            playBtn.style.display = 'flex';
            audioPlayer.currentTime = 0;
            audioProgressFill.style.width = '0%';
        });

        // Handle audio metadata loaded
        audioPlayer.addEventListener('loadedmetadata', function() {
            audioDuration.textContent = formatTime(audioPlayer.duration);
            audioCurrentTime.textContent = '0:00';
        });

        // Handle audio load error
        audioPlayer.addEventListener('error', function() {
            console.log('Audio file not found or unable to load');
        });

        // ============ Original Functions ============
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
            document.getElementById('audio-player').src = 'api/audio/' + conversationId;
            // Reset player state
            stopAudio();
            // Initialize duration display
            audioCurrentTime.textContent = '0:00';
            audioDuration.textContent = '0:00';
            UIkit.modal(document.getElementById('audio-modal')).show();
        }

        // Initialize on page load
        document.addEventListener('DOMContentLoaded', function() {
            calculateStats();
            updateFilterBadge();
            
            // Update filter badge when inputs change
            document.querySelectorAll('#conversationId, #phone, #type, #score, #audio').forEach(input => {
                input.addEventListener('change', updateFilterBadge);
                input.addEventListener('input', updateFilterBadge);
            });
            
            // ...existing code...
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