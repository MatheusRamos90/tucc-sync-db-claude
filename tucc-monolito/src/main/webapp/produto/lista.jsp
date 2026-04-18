<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-tag"></span> Produtos
        <a href="${pageContext.request.contextPath}/produto/form.jsp"
           class="btn btn-primary pull-right">
            <span class="glyphicon glyphicon-plus"></span> Novo Produto
        </a>
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body" style="padding:0">
        <table id="tabela-produtos" class="table table-striped table-bordered table-hover" style="margin-bottom:0">
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
            <tr>
                <td colspan="7" class="text-center text-muted" style="padding:20px">
                    <span class="glyphicon glyphicon-refresh"></span> Carregando...
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>

<script>
var ctxPath = '${pageContext.request.contextPath}';

$(document).ready(function () {

    carregarProdutos();

    function carregarProdutos() {
        $.ajax({
            url: ctxPath + '/api/produto',
            type: 'GET',
            dataType: 'json',
            success: function (dados) {
                var tbody = $('#tabela-produtos tbody');
                tbody.empty();
                if (dados.length === 0) {
                    tbody.append('<tr><td colspan="7" class="text-center text-muted" style="padding:20px">Nenhum produto cadastrado.</td></tr>');
                    return;
                }
                $.each(dados, function (i, p) {
                    tbody.append(
                        '<tr>' +
                        '<td>' + p.id + '</td>' +
                        '<td>' + escapeHtml(p.nome) + '</td>' +
                        '<td class="text-right">' + formatDecimal(p.valor) + '</td>' +
                        '<td class="text-right">' + (p.desconto != null ? formatDecimal(p.desconto) : '<span class="text-muted">&mdash;</span>') + '</td>' +
                        '<td>' + formatDate(p.dtCriacao) + '</td>' +
                        '<td>' + formatDate(p.dtAtualizacao) + '</td>' +
                        '<td class="text-center">' +
                            '<a href="' + ctxPath + '/produto/form.jsp?id=' + p.id + '" class="btn btn-xs btn-default" title="Editar">' +
                                '<span class="glyphicon glyphicon-pencil"></span>' +
                            '</a> ' +
                            '<button class="btn btn-xs btn-danger btn-excluir" ' +
                                    'data-id="' + p.id + '" data-nome="' + escapeAttr(p.nome) + '" title="Excluir">' +
                                '<span class="glyphicon glyphicon-trash"></span>' +
                            '</button>' +
                        '</td>' +
                        '</tr>'
                    );
                });
            },
            error: function () {
                $('#tabela-produtos tbody').html('<tr><td colspan="7" class="text-center text-danger" style="padding:20px">Erro ao carregar produtos.</td></tr>');
            }
        });
    }

    $(document).on('click', '.btn-excluir', function () {
        var id   = $(this).data('id');
        var nome = $(this).data('nome');
        if (!confirm('Deseja excluir o produto "' + nome + '"?')) return;

        $.ajax({
            url: ctxPath + '/api/produto?id=' + id,
            type: 'DELETE',
            dataType: 'json',
            success: function (resp) {
                exibirAlerta(resp.success ? 'success' : 'danger', resp.message);
                if (resp.success) carregarProdutos();
            },
            error: function () {
                exibirAlerta('danger', 'Erro ao excluir produto.');
            }
        });
    });

    function exibirAlerta(tipo, mensagem) {
        $('#alerts').html(
            '<div class="alert alert-' + tipo + ' alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span></button>' +
            mensagem +
            '</div>'
        );
        setTimeout(function () {
            $('#alerts .alert').fadeOut(400, function () { $(this).remove(); });
        }, 3000);
    }

    function formatDecimal(val) {
        if (val == null) return '';
        return parseFloat(val).toLocaleString('pt-BR', {minimumFractionDigits: 2, maximumFractionDigits: 2});
    }

    function formatDate(iso) {
        if (!iso) return '';
        var d = new Date(iso);
        return pad(d.getDate()) + '/' + pad(d.getMonth() + 1) + '/' + d.getFullYear() +
               ' ' + pad(d.getHours()) + ':' + pad(d.getMinutes());
    }

    function pad(n) { return n < 10 ? '0' + n : '' + n; }

    function escapeHtml(str) { return $('<div>').text(str || '').html(); }

    function escapeAttr(str) { return (str || '').replace(/"/g, '&quot;'); }
});
</script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
