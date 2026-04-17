<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-link"></span> Produto-Empresa
        <a href="${pageContext.request.contextPath}/produto-empresa/novo.action"
           class="btn btn-primary pull-right">
            <span class="glyphicon glyphicon-plus"></span> Nova Associação
        </a>
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body" style="padding:0">
        <table class="table table-striped table-bordered table-hover" style="margin-bottom:0">
            <thead>
            <tr>
                <th style="width:60px">ID</th>
                <th>Produto</th>
                <th>Empresa</th>
                <th style="width:80px" class="text-center">Ações</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty associacoes}">
                    <tr>
                        <td colspan="4" class="text-center text-muted" style="padding:20px">
                            Nenhuma associação cadastrada.
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${associacoes}" var="pe">
                        <tr>
                            <td>${pe.id}</td>
                            <td>
                                <span class="label label-default">#${pe.produtoId}</span>
                                <c:out value="${pe.produtoNome}"/>
                            </td>
                            <td>
                                <span class="label label-default">#${pe.empresaId}</span>
                                <c:out value="${pe.empresaNome}"/>
                            </td>
                            <td class="text-center">
                                <a href="${pageContext.request.contextPath}/produto-empresa/excluir.action?id=${pe.id}"
                                   class="btn btn-xs btn-danger" title="Excluir"
                                   onclick="return confirm('Deseja excluir esta associação?')">
                                    <span class="glyphicon glyphicon-trash"></span>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
