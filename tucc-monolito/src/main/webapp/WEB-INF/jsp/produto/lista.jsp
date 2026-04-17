<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="pt_BR"/>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-tag"></span> Produtos
        <a href="${pageContext.request.contextPath}/produto/novo.action"
           class="btn btn-primary pull-right">
            <span class="glyphicon glyphicon-plus"></span> Novo Produto
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
                <th style="width:120px">Valor (R$)</th>
                <th style="width:120px">Desconto (R$)</th>
                <th style="width:140px">Criação</th>
                <th style="width:140px">Atualização</th>
                <th style="width:130px" class="text-center">Ações</th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <c:when test="${empty produtos}">
                    <tr>
                        <td colspan="7" class="text-center text-muted" style="padding:20px">
                            Nenhum produto cadastrado.
                        </td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${produtos}" var="p">
                        <tr>
                            <td>${p.id}</td>
                            <td><c:out value="${p.nome}"/></td>
                            <td class="text-right">
                                <fmt:formatNumber value="${p.valor}" pattern="#,##0.00"/>
                            </td>
                            <td class="text-right">
                                <c:choose>
                                    <c:when test="${not empty p.desconto}">
                                        <fmt:formatNumber value="${p.desconto}" pattern="#,##0.00"/>
                                    </c:when>
                                    <c:otherwise><span class="text-muted">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${not empty p.dtCriacao}">
                                    <fmt:formatDate value="${p.dtCriacao}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:if>
                            </td>
                            <td>
                                <c:if test="${not empty p.dtAtualizacao}">
                                    <fmt:formatDate value="${p.dtAtualizacao}" pattern="dd/MM/yyyy HH:mm"/>
                                </c:if>
                            </td>
                            <td class="text-center">
                                <a href="${pageContext.request.contextPath}/produto/editar.action?id=${p.id}"
                                   class="btn btn-xs btn-default" title="Editar">
                                    <span class="glyphicon glyphicon-pencil"></span>
                                </a>
                                <a href="${pageContext.request.contextPath}/produto/excluir.action?id=${p.id}"
                                   class="btn btn-xs btn-danger" title="Excluir"
                                   onclick="return confirm('Deseja excluir o produto &quot;<c:out value="${p.nome}"/>&quot;?')">
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
