<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-tag"></span>
        <c:choose>
            <c:when test="${not empty produto.id}">Editar Produto #${produto.id}</c:when>
            <c:otherwise>Novo Produto</c:otherwise>
        </c:choose>
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
        <form action="${pageContext.request.contextPath}/produto/salvar.action"
              method="post" class="form-horizontal">

            <c:if test="${not empty produto.id}">
                <input type="hidden" name="produto.id" value="${produto.id}"/>
            </c:if>

            <div class="form-group">
                <label class="col-sm-2 control-label">Nome <span class="text-danger">*</span></label>
                <div class="col-sm-6">
                    <input type="text" name="produto.nome" class="form-control"
                           value="<c:out value='${produto.nome}'/>"
                           placeholder="Nome do produto" maxlength="255" required/>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">Valor (R$) <span class="text-danger">*</span></label>
                <div class="col-sm-4">
                    <input type="text" name="produto.valor" id="valor" class="form-control"
                           value="${produto.valor}"
                           placeholder="Ex: 99.90"/>
                    <span class="help-block">Use ponto como separador decimal (ex: 99.90)</span>
                </div>
            </div>

            <div class="form-group">
                <label class="col-sm-2 control-label">Desconto (R$)</label>
                <div class="col-sm-4">
                    <input type="text" name="produto.desconto" id="desconto" class="form-control"
                           value="${produto.desconto}"
                           placeholder="Ex: 10.00 (opcional)"/>
                    <span class="help-block">Use ponto como separador decimal. Deixe em branco se não houver desconto.</span>
                </div>
            </div>

            <div class="form-group">
                <div class="col-sm-offset-2 col-sm-6">
                    <button type="submit" class="btn btn-primary">
                        <span class="glyphicon glyphicon-floppy-disk"></span>
                        <c:choose>
                            <c:when test="${not empty produto.id}">Atualizar</c:when>
                            <c:otherwise>Cadastrar</c:otherwise>
                        </c:choose>
                    </button>
                    <a href="${pageContext.request.contextPath}/produto/listar.action"
                       class="btn btn-default">
                        <span class="glyphicon glyphicon-arrow-left"></span> Voltar
                    </a>
                </div>
            </div>

        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
