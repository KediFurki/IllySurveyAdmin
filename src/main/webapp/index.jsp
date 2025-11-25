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
    <title>Illy - Gestione Sondaggi</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="card shadow">
        <div class="card-header bg-danger text-white">
            <h3 class="mb-0">Illy - Report Sondaggi</h3>
        </div>
        <div class="card-body">
            
            <form action="admin" method="get" class="row g-3 mb-4 align-items-end p-3 bg-white border rounded">
                <div class="col-auto">
                    <label class="form-label fw-bold">Dal (Data Inizio)</label>
                    <input type="date" class="form-control" name="startDate" value="${startDate}">
                </div>
                <div class="col-auto">
                    <label class="form-label fw-bold">Al (Data Fine)</label>
                    <input type="date" class="form-control" name="endDate" value="${endDate}">
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary"><i class="bi bi-search"></i> Filtra</button>
                    <a href="admin?action=export&startDate=${startDate}&endDate=${endDate}" class="btn btn-success">
                        <i class="bi bi-file-earmark-spreadsheet"></i> Esporta CSV
                    </a>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-hover align-middle">
                    <thead class="table-dark">
                        <tr>
                            <th>Data</th>
                            <th>ID Interazione</th>
                            <th>Telefono</th>
                            <th>Tipo</th>
                            <th>Voto</th>
                            <th>Audio</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:if test="${empty reportList}">
                            <tr><td colspan="6" class="text-center text-muted">Nessun dato trovato.</td></tr>
                        </c:if>
                        <c:forEach var="item" items="${reportList}">
                            <tr>
                                <td>${item.date}</td>
                                <td>${item.conversationId}</td>
                                <td>${item.customerPhone}</td>
                                <td>${item.type != null ? item.type : '-'}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${item.score < 6}"><span class="badge bg-danger rounded-pill">${item.score}</span></c:when>
                                        <c:otherwise><span class="badge bg-success rounded-pill">${item.score}</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${item.hasAudio == 'true' ? '<span class="badge bg-warning text-dark">Sì</span>' : 'No'}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
</body>
</html>