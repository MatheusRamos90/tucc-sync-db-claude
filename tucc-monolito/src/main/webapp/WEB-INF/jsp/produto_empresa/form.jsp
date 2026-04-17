<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-link"></span> Nova Associação Produto-Empresa
    </h2>
</div>

<s:if test="hasFieldErrors()">
    <div class="alert alert-danger">
        <strong>Corrija os erros abaixo:</strong>
        <s:fielderror/>
    </div>
</s:if>

<div class="panel panel-default">
    <div class="panel-body">
        <form action="${pageContext.request.contextPath}/produto-empresa/salvar.action"
              method="post" class="form-horizontal">

            <div class="form-group">
                <label class="col-sm-2 control-label">Produto <span class="text-danger">*</span></label>
                <div class="col-sm-5">
                    <select name="produtoEmpresa.produtoId" class="form-control" required>
                        <option value="">-- Selecione um produto --</option>
                        <c:forEach items="${produtos}" var="p">
                            <option value="${p.id}"
                                ${produtoEmpresa.produtoId == p.id ? 'selected' : ''}>
                                #${p.id} — <c:out value="${p.nome}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty produtos}">
                        <span class="help-block text-warning">
                            Nenhum produto cadastrado.
                            <a href="${pageContext.request.contextPath}/produto/novo.action">Cadastrar produto</a>
                        </span>
                    </c:if>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">Empresa <span class="text-danger">*</span></label>
                <div class="col-sm-5">
                    <select name="produtoEmpresa.empresaId" class="form-control" required>
                        <option value="">-- Selecione uma empresa --</option>
                        <c:forEach items="${empresas}" var="e">
                            <option value="${e.id}"
                                ${produtoEmpresa.empresaId == e.id ? 'selected' : ''}>
                                #${e.id} — <c:out value="${e.nome}"/>
                            </option>
                        </c:forEach>
                    </select>
                    <c:if test="${empty empresas}">
                        <span class="help-block text-warning">
                            Nenhuma empresa cadastrada.
                            <a href="${pageContext.request.contextPath}/empresa/novo.action">Cadastrar empresa</a>
                        </span>
                    </c:if>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-6">
                    <button type="submit" class="btn btn-primary"
                            ${empty produtos or empty empresas ? 'disabled' : ''}>
                        <span class="glyphicon glyphicon-floppy-disk"></span> Cadastrar
                    </button>
                    <a href="${pageContext.request.contextPath}/produto-empresa/listar.action"
                       class="btn btn-default">
                        <span class="glyphicon glyphicon-arrow-left"></span> Voltar
                    </a>
                </div>
            </div>

        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
