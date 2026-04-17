<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="pt_BR"/>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-briefcase"></span> Empresas
        <a href="${pageContext.request.contextPath}/empresa/novo.action"
           class="btn btn-primary pull-right">
            <span class="glyphicon glyphicon-plus"></span> Nova Empresa
        </a>
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body" style="padding:0">
        <table class="table table-striped table-bordered table-hover" style="margin-bottom:0">
            <thead>
            <tr>
                <th style="width:60px">ID</th>
                <th>Nome</th>
                <th style="width:160px">CNPJ</th>
                <th style="width:140px">Criação</th>
                <th style="width:140px">Atualização</th>
                <th style="width:100px" class="text-center">Ações</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty empresas}">
                    <tr>
                        <td colspan="6" class="text-center text-muted" style="padding:20px">
                            Nenhuma empresa cadastrada.
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${empresas}" var="e">
                        <tr>
                            <td>${e.id}</td>
                            <td><c:out value="${e.nome}"/></td>
                            <td><c:out value="${e.cnpj}"/></td>
                            <td>
                                <c:if test="${not empty e.dtCriacao}">
                                    <fmt:formatDate value="${e.dtCriacao}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${not empty e.dtAtualizacao}">
                                    <fmt:formatDate value="${e.dtAtualizacao}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:if>
                            </td>
                            <td class="text-center">
                                <a href="${pageContext.request.contextPath}/empresa/editar.action?id=${e.id}"
                                   class="btn btn-xs btn-default" title="Editar">
                                    <span class="glyphicon glyphicon-pencil"></span>
                                </a>
                                <a href="${pageContext.request.contextPath}/empresa/excluir.action?id=${e.id}"
                                   class="btn btn-xs btn-danger" title="Excluir"
                                   onclick="return confirm('Deseja excluir a empresa &quot;<c:out value="${e.nome}"/>&quot;?')">
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
