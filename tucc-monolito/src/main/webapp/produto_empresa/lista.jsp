<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp"/>

<div class="page-header">
    <h2>
        <span class="glyphicon glyphicon-link"></span> Produto-Empresa
        <a href="${pageContext.request.contextPath}/produto_empresa/form.jsp"
           class="btn btn-primary pull-right">
            <span class="glyphicon glyphicon-plus"></span> Nova Associação
        </a>
    </h2>
</div>

<div class="panel panel-default">
    <div class="panel-body" style="padding:0">
        <table id="tabela-associacoes" class="table table-striped table-bordered table-hover" style="margin-bottom:0">
            <thead>
            <tr>
                <th style="width:60px">ID</th>
                <th>Produto</th>
                <th>Empresa</th>
                <th style="width:80px" class="text-center">Ações</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td colspan="4" class="text-center text-muted" style="padding:20px">
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

    carregarAssociacoes();

    function carregarAssociacoes() {
        $.ajax({
            url: ctxPath + '/api/produto-empresa',
            type: 'GET',
            dataType: 'json',
            success: function (dados) {
                var tbody = $('#tabela-associacoes tbody');
                tbody.empty();
                if (dados.length === 0) {
                    tbody.append('<tr><td colspan="4" class="text-center text-muted" style="padding:20px">Nenhuma associação cadastrada.</td></tr>');
                    return;
                }
                $.each(dados, function (i, pe) {
                    tbody.append(
                        '<tr>' +
                        '<td>' + pe.id + '</td>' +
                        '<td><span class="label label-default">#' + pe.produtoId + '</span> ' + escapeHtml(pe.produtoNome) + '</td>' +
                        '<td><span class="label label-default">#' + pe.empresaId + '</span> ' + escapeHtml(pe.empresaNome) + '</td>' +
                        '<td class="text-center">' +
                            '<button class="btn btn-xs btn-danger btn-excluir" data-id="' + pe.id + '" title="Excluir">' +
                                '<span class="glyphicon glyphicon-trash"></span>' +
                            '</button>' +
                        '</td>' +
                        '</tr>'
                    );
                });
            },
            error: function () {
                $('#tabela-associacoes tbody').html('<tr><td colspan="4" class="text-center text-danger" style="padding:20px">Erro ao carregar associações.</td></tr>');
            }
        });
    }

    $(document).on('click', '.btn-excluir', function () {
        var id = $(this).data('id');
        if (!confirm('Deseja excluir esta associação?')) return;

        $.ajax({
            url: ctxPath + '/api/produto-empresa?id=' + id,
            type: 'DELETE',
            dataType: 'json',
            success: function (resp) {
                exibirAlerta(resp.success ? 'success' : 'danger', resp.message);
                if (resp.success) carregarAssociacoes();
            },
            error: function () {
                exibirAlerta('danger', 'Erro ao excluir associação.');
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

    function escapeHtml(str) { return $('<div>').text(str || '').html(); }
});
</script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp"/>
