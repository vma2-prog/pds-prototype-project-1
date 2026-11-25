**Trabalho de P.D.S.**

**BESTAND**

Alunos: Samyr Mello, Kauã Eder, Vitor Marques e Riquelme Pinheiro

Turma: 913-A

Professor: Leonardo Medeiros

Maceió \- Al
2026

# **Descrição do Software**

Bestand é um sistema de gerenciamento de estoque direcionado a ser usufruído por pequenos empreendedores. Neste software, um cliente (dono de um micro-empreendimento) realiza o cadastro de seu negócio, e logo possui acesso a um serviço que permite que ele gerencie o escopo logístico de seus produtos \- como a entrada e saída de mercadorias, controle de estoque e informações sobre distribuição \- auxiliando na automatização a administração que o empresário possui com relação à seu projeto.

# **Requisitos do Sistema**

* # Requisitos Funcionais:

  *RF01 — Cadastro, alteração e remoção de perfis de usuário.*  
  *RF02 — Sistema de permissões para perfis.*  
  *RF03 — Cadastro, alteração e remoção de produtos.*  
  *RF04 — Registro de vendas e devoluções de produtos no histórico.*  
  *RF05 — Registro de carregamento para aumentar estoque de produtos.*  
  *RF06 — Cálculo de receita e custo totais por janela de tempo.*

* # Requisitos Não Funcionais:

  # *RNF01 — Banco de dados SQLite para armazenar informações na máquina*

  *RNF02 — Linguagem: Java 17*  
  *RNF03 — Interface gráfica: JavaFX 17.0.2*  
  *RNF04 — Gerenciador de dependências: Maven*

# **Casos de Uso** 

* ## **UC001** – Cadastrar perfil de usuário

**Descrição** – Cadastrar um novo perfil de usuário no sistema, definindo seu nome, senha e permissões.

**Requisitos** – RF01; RF02.  
**Ator** – Administrador.  
**Pré-condição** – Administrador logado no sistema.  
**Pós-condição** – Novo perfil de usuário cadastrado no sistema.

### **Fluxo principal**

1. O administrador solicita Cadastrar perfil de usuário.  
2. O sistema mostra uma interface solicitando o nome, a senha e as permissões do perfil.  
3. O administrador insere o nome, a senha, seleciona as permissões desejadas e clica em Cadastrar. \[UC001.FS001\]; \[UC001.FS002\]; \[UC001.FS003\]  
4. O sistema verifica as informações fornecidas.  
5. O sistema cadastra o novo perfil e mostra uma mensagem dizendo que o perfil foi cadastrado com sucesso.

### **Fluxos alternativos**

**\[UC001.FS001\]** – Nome de perfil inválido

1. O administrador solicita Cadastrar perfil de usuário.  
2. O sistema mostra uma interface solicitando o nome, a senha e as permissões do perfil.  
3. O administrador insere um nome contendo espaços, acentos ou caracteres não permitidos e clica em Cadastrar.  
4. O sistema mostra uma mensagem informando que o nome do perfil contém caracteres inválidos.  
5. O sistema volta para a interface de cadastro.  
6. O administrador corrige o nome e clica em Cadastrar.  
7. O sistema cadastra o novo perfil e mostra uma mensagem dizendo que o perfil foi cadastrado com sucesso.


**\[UC001.FS002\]** – Nome de perfil já cadastrado

1. O administrador solicita Cadastrar perfil de usuário.  
2. O sistema mostra uma interface solicitando o nome, a senha e as permissões do perfil.  
3. O administrador insere um nome já utilizado por outro perfil e clica em Cadastrar.  
4. O sistema mostra uma mensagem informando que já existe um perfil com o nome informado.  
5. O sistema volta para a interface de cadastro.  
6. O administrador informa outro nome e clica em Cadastrar.  
7. O sistema cadastra o novo perfil e mostra uma mensagem dizendo que o perfil foi cadastrado com sucesso.

**\[UC001.FS003\]** – Faltando alguma informação obrigatória

1. O administrador solicita Cadastrar perfil de usuário.  
2. O sistema mostra uma interface solicitando o nome, a senha e as permissões do perfil.  
3. O administrador não preenche uma ou mais informações obrigatórias e clica em Cadastrar.  
4. O sistema mostra uma mensagem de erro e indica os campos que precisam ser preenchidos.  
5. O administrador preenche os campos restantes e clica em Cadastrar.  
6. O sistema cadastra o novo perfil e mostra uma mensagem dizendo que o perfil foi cadastrado com sucesso.

**\[UC001.FS004\]** – Cancelar cadastro

1. O administrador solicita Cadastrar perfil de usuário.  
2. O sistema mostra uma interface solicitando o nome, a senha e as permissões do perfil.  
3. O administrador clica em Cancelar.  
4. O sistema cancela o cadastro e retorna à interface anterior, sem salvar as informações inseridas.

* ## **UC002 – Alterar informações do perfil de usuário**

**Descrição** – Alterar a senha e as permissões de um perfil de usuário cadastrado no sistema.

**Requisitos** – RF01; RF02.  
**Ator** – Administrador.  
**Pré-condição** – Administrador logado no sistema; perfil de usuário cadastrado no sistema.  
**Pós-condição** – Informações do perfil de usuário alteradas.

### **Fluxo principal**

1. O administrador solicita Alterar informações do perfil de usuário.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em Procurar. \[UC002.FS001\]  
4. O sistema mostra uma interface com as informações do perfil para o administrador alterar. \[UC002.FS002\]  
5. O administrador altera a senha e/ou as permissões desejadas e clica em Confirmar. \[UC002.FS003\]  
6. O sistema salva as alterações e mostra uma mensagem dizendo que as alterações foram feitas com sucesso.

### **Fluxos alternativos**

**\[UC002.FS001\] – Perfil não cadastrado no sistema**

1. O administrador solicita **Alterar informações do perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em **Procurar**.  
4. O sistema não encontra o perfil e mostra uma mensagem dizendo que o perfil não está cadastrado no sistema.  
5. O sistema volta para a interface pedindo o nome do perfil.

**\[UC002.FS002\] – Perfil administrador**

1. O administrador solicita **Alterar informações do perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome admin e clica em **Procurar**.  
4. O sistema identifica que o perfil informado é o perfil administrador.  
5. O sistema mostra uma mensagem informando que o perfil admin não pode ser alterado.  
6. O sistema volta para a interface pedindo o nome do perfil.

**\[UC002.FS003\] – Faltando alguma informação obrigatória**

1. O administrador solicita **Alterar informações do perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em **Procurar**.  
4. O sistema mostra uma interface com as informações do perfil para o administrador alterar.  
5. O administrador remove uma informação obrigatória e clica em **Confirmar**.  
6. O sistema mostra uma mensagem de erro e pede para preencher o(s) campo(s) restante(s).  
7. O administrador preenche o(s) campo(s) restante(s) e clica em **Confirmar**.  
8. O sistema salva as alterações e mostra uma mensagem dizendo que as alterações foram feitas com sucesso.

**\[UC002.FS004\] – Cancelar alteração**

1. O administrador solicita **Alterar informações do perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em **Procurar**.  
4. O sistema mostra uma interface com as informações do perfil para o administrador alterar.  
5. O administrador clica em **Cancelar**.  
6. O sistema cancela a alteração e retorna à interface anterior, sem salvar as alterações realizadas.

* ## **UC003 –** Remover perfil de usuário

**Descrição** – Remover um perfil de usuário cadastrado no sistema.

**Requisitos** – RF01.  
**Ator** – Administrador.  
**Pré-condição** – Administrador logado no sistema; perfil de usuário cadastrado no sistema.  
**Pós-condição** – Perfil de usuário removido do sistema.

### **Fluxo principal**

1. O administrador solicita **Remover perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em **Procurar**. \[UC003.FS001\]  
4. O sistema mostra as informações do perfil e solicita a confirmação da remoção. \[UC003.FS002\]  
5. O administrador confirma a remoção.  
6. O sistema remove o perfil e mostra uma mensagem dizendo que o perfil foi removido com sucesso.

### **Fluxos alternativos**

**\[UC003.FS001\] – Perfil não cadastrado no sistema**

1. O administrador solicita **Remover perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em **Procurar**.  
4. O sistema não encontra o perfil e mostra uma mensagem dizendo que o perfil não está cadastrado no sistema.  
5. O sistema volta para a interface pedindo o nome do perfil.

**\[UC003.FS002\] – Perfil administrador**

1. O administrador solicita **Remover perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome admin e clica em **Procurar**.  
4. O sistema identifica que o perfil informado é o perfil administrador.  
5. O sistema mostra uma mensagem informando que o perfil admin não pode ser removido.  
6. O sistema volta para a interface pedindo o nome do perfil.

**\[UC003.FS003\] – Cancelar remoção**

1. O administrador solicita **Remover perfil de usuário**.  
2. O sistema mostra uma interface solicitando o nome do perfil.  
3. O administrador insere o nome do perfil e clica em **Procurar**.  
4. O sistema mostra as informações do perfil e solicita a confirmação da remoção.  
5. O administrador clica em **Cancelar**.  
6. O sistema cancela a remoção e retorna à interface anterior, sem remover o perfil.

* ## **UC004** – Cadastrar produto

**Descrição** – Cadastrar um novo produto no catálogo do sistema.  
**Requisitos** – RF03, RF02.  
**Ator** – Usuário com permissão para cadastrar produtos.  
**Pré-condição** – Usuário logado no sistema; usuário possui permissão para cadastrar produtos.  
**Pós-condição** – Novo produto cadastrado no catálogo, com quantidade em estoque igual a zero.

### **Fluxo principal**

1. O usuário solicita **Cadastrar produto**.  
2. O sistema mostra uma interface solicitando o nome, o código e o preço do produto.  
3. O usuário insere as informações e clica em **Cadastrar**. \[UC004.FS001\]; \[UC004.FS002\]; \[UC004.FS003\]  
4. O sistema gera um ID para o produto.  
5. O sistema cadastra o produto com a quantidade em estoque igual a zero.  
6. O sistema mostra uma mensagem dizendo que o produto foi cadastrado com sucesso.

### **Fluxos alternativos**

**\[UC004.FS001\] – Código de produto já cadastrado**

1. O usuário solicita **Cadastrar produto**.  
2. O sistema mostra uma interface solicitando o nome, o código e o preço do produto.  
3. O usuário insere um código já utilizado por outro produto e clica em **Cadastrar**.  
4. O sistema mostra uma mensagem informando que já existe um produto com o código informado.  
5. O sistema volta para a interface de cadastro.  
6. O usuário informa outro código e clica em **Cadastrar**.  
7. O sistema gera um ID para o produto.  
8. O sistema cadastra o produto com a quantidade em estoque igual a zero.  
9. O sistema mostra uma mensagem dizendo que o produto foi cadastrado com sucesso.

**\[UC004.FS002\] – Faltando alguma informação obrigatória**

1. O usuário solicita **Cadastrar produto**.  
2. O sistema mostra uma interface solicitando o nome, o código e o preço do produto.  
3. O usuário não preenche uma ou mais informações obrigatórias e clica em **Cadastrar**.  
4. O sistema mostra uma mensagem de erro e indica os campos que precisam ser preenchidos.  
5. O usuário preenche os campos restantes e clica em **Cadastrar**.  
6. O sistema gera um ID para o produto.  
7. O sistema cadastra o produto com a quantidade em estoque igual a zero.  
8. O sistema mostra uma mensagem dizendo que o produto foi cadastrado com sucesso.

**\[UC004.FS003\] – Cancelar cadastro**

1. O usuário solicita **Cadastrar produto**.  
2. O sistema mostra uma interface solicitando o nome, o código e o preço do produto.  
3. O usuário clica em **Cancelar**.  
4. O sistema cancela o cadastro e retorna à interface anterior, sem salvar as informações inseridas.

**\[UC005.FS004\] \- Usuário não possui permissão para realizar cadastro**

1. O usuário solicita **Cadastrar produto**.  
2. O sistema mostra uma interface solicitando o nome, o código e o preço do produto.  
3. O usuário não possui as permissões necessárias para a realização do cadastro.  
4. O sistema exibe uma mensagem de erro informando que o usuário não tem acesso em solicitar essa função.  
5. O sistema cancela o cadastro e retorna à interface anterior, sem nenhuma informação salva.  
   

* ## **UC005** – Alterar produto

**Descrição** – Alterar as informações de um produto cadastrado no catálogo.

**Requisitos** – RF03, RF02.  
**Ator** – Usuário com permissão para alterar produtos.  
**Pré-condição** – Usuário logado no sistema; usuário possui permissão para alterar produtos; produto cadastrado no sistema.  
**Pós-condição** – Informações do produto alteradas.

### **Fluxo principal**

1. O usuário solicita **Alterar produto**.  
2. O sistema mostra uma lista com todos os produtos cadastrados.  
3. O usuário seleciona o produto que deseja alterar.  
4. O sistema mostra uma interface com as informações do produto.  
5. O usuário altera o nome, o código e/ou o preço do produto e clica em **Confirmar**. \[UC005.FS001\]; \[UC005.FS002\]  
6. O sistema salva as alterações e mostra uma mensagem dizendo que as alterações foram feitas com sucesso.

### **Fluxos alternativos**

**\[UC005.FS001\] – Código de produto já cadastrado**

1. O usuário solicita **Alterar produto**.  
2. O sistema mostra uma lista com todos os produtos cadastrados.  
3. O usuário seleciona o produto que deseja alterar.  
4. O sistema mostra uma interface com as informações do produto.  
5. O usuário altera o código para um código já utilizado por outro produto e clica em **Confirmar**.  
6. O sistema mostra uma mensagem informando que já existe um produto com o código informado.  
7. O sistema volta para a interface de alteração.  
8. O usuário informa outro código e clica em **Confirmar**.  
9. O sistema salva as alterações e mostra uma mensagem dizendo que as alterações foram feitas com sucesso.

**\[UC005.FS002\] – Faltando alguma informação obrigatória**

1. O usuário solicita **Alterar produto**.  
2. O sistema mostra uma lista com todos os produtos cadastrados.  
3. O usuário seleciona o produto que deseja alterar.  
4. O sistema mostra uma interface com as informações do produto.  
5. O usuário remove uma ou mais informações obrigatórias e clica em **Confirmar**.  
6. O sistema mostra uma mensagem de erro e indica os campos que precisam ser preenchidos.  
7. O usuário preenche os campos restantes e clica em **Confirmar**.  
8. O sistema salva as alterações e mostra uma mensagem dizendo que as alterações foram feitas com sucesso.

**\[UC005.FS003\] – Cancelar alteração**

1. O usuário solicita **Alterar produto**.  
2. O sistema mostra uma lista com todos os produtos cadastrados.  
3. O usuário seleciona o produto que deseja alterar.  
4. O sistema mostra uma interface com as informações do produto.  
5. O usuário altera as informações desejadas e clica em **Cancelar**.  
6. O sistema cancela a alteração e retorna à interface anterior, sem salvar as alterações realizadas.

* ## **UC006** – Remover produto

**Descrição** – Remover um produto cadastrado no catálogo.

**Requisitos** – RF03, RF02.  
**Ator** – Usuário com permissão para remover produtos.

**Pré-condição** – Usuário logado no sistema; usuário possui permissão para remover produtos; produto cadastrado no sistema.  
**Pós-condição** – Produto removido do catálogo.

### **Fluxo principal**

1. O usuário solicita **Remover produto**.  
2. O sistema mostra uma lista com todos os produtos cadastrados.  
3. O usuário seleciona o produto que deseja remover.  
4. O sistema mostra uma interface com as informações do produto.  
5. O usuário seleciona a opção **Remover**.  
6. O sistema mostra uma mensagem informando que a remoção é uma ação destrutiva e solicita a confirmação da remoção. \[UC006.FS001\]  
7. O usuário confirma a remoção.  
8. O sistema remove o produto e mostra uma mensagem dizendo que o produto foi removido com sucesso.

### **Fluxos alternativos**

**\[UC006.FS001\] – Cancelar remoção**

1. O usuário solicita **Remover produto**.  
2. O sistema mostra uma lista com todos os produtos cadastrados.  
3. O usuário seleciona o produto que deseja remover.  
4. O sistema mostra uma interface com as informações do produto.  
5. O usuário seleciona a opção **Remover**.  
6. O sistema mostra uma mensagem informando que a remoção é uma ação destrutiva e solicita a confirmação da remoção.  
7. O usuário cancela a remoção.  
8. O sistema cancela a operação e retorna à interface anterior, sem remover o produto.

* ## **UC007** – Registrar venda

**Descrição** – Registrar uma venda de um ou mais produtos, atualizando automaticamente suas quantidades em estoque e armazenando a venda no histórico.

**Requisitos** – RF04, RF02.  
**Ator** – Usuário com permissão para registrar vendas.  
**Pré-condição** – Usuário logado no sistema; usuário possui permissão para registrar vendas; produtos cadastrados no sistema.  
**Pós-condição** – Venda registrada no histórico; quantidade dos produtos vendidos reduzida no estoque.

### **Fluxo principal**

1. O usuário solicita **Registrar venda**.  
2. O sistema mostra uma interface para selecionar os produtos da venda.  
3. O usuário pesquisa um produto pelo nome ou código e o seleciona. \[UC007.FS001\]  
4. O sistema adiciona o produto à venda.  
5. O usuário informa a quantidade desejada. \[UC007.FS002\]  
6. O sistema verifica a disponibilidade do produto em estoque.  
7. O usuário adiciona outros produtos à venda, repetindo os passos 3 a 6, caso necessário.  
8. O usuário seleciona o método de pagamento.  
9. O sistema mostra os produtos selecionados, suas quantidades, os preços, o valor total e o método de pagamento e solicita a confirmação da venda.  
10. O usuário confirma a venda.  
11. O sistema registra a venda no histórico, armazenando os produtos, suas quantidades, os preços, o valor total, a data e hora, o usuário responsável pelo registro e o método de pagamento.  
12. O sistema reduz do estoque as quantidades dos produtos vendidos.  
13. O sistema mostra uma mensagem dizendo que a venda foi registrada com sucesso.

### **Fluxos alternativos**

**\[UC007.FS001\] – Produto não encontrado**

1. O usuário solicita **Registrar venda**.  
2. O sistema mostra uma interface para selecionar os produtos da venda.  
3. O usuário pesquisa um produto pelo nome ou código.  
4. O sistema não encontra nenhum produto correspondente à pesquisa.  
5. O sistema mostra uma mensagem informando que nenhum produto foi encontrado.  
6. O sistema retorna à interface de seleção de produtos.

**\[UC007.FS002\] – Quantidade superior ao estoque disponível**

1. O usuário solicita **Registrar venda**.  
2. O sistema mostra uma interface para selecionar os produtos da venda.  
3. O usuário seleciona um produto.  
4. O sistema mostra o produto e sua quantidade disponível em estoque.  
5. O usuário informa uma quantidade superior à disponível em estoque.  
6. O sistema informa que não há estoque suficiente para realizar a venda.  
7. O sistema retorna à interface da venda para que o usuário informe uma quantidade válida.

**\[UC007.FS003\] – Cancelar venda**

1. O usuário solicita **Registrar venda**.  
2. O sistema mostra uma interface para registrar a venda.  
3. O usuário seleciona os produtos, suas quantidades e o método de pagamento.  
4. O sistema mostra os dados da venda e solicita a confirmação.  
5. O usuário seleciona **Cancelar**.  
6. O sistema cancela a operação sem registrar a venda e sem alterar o estoque.

**\[UC007.FS004\] – Venda não confirmada**

1. O usuário solicita **Registrar venda**.  
2. O sistema mostra uma interface para registrar a venda.  
3. O usuário seleciona os produtos, suas quantidades e o método de pagamento.  
4. O sistema mostra os dados da venda e solicita a confirmação.  
5. O usuário não confirma a venda.  
6. O sistema retorna à interface da venda, sem registrar a venda e sem alterar o estoque.

* ## **UC008** – Registrar devolução

**Descrição** – Registrar a devolução total ou parcial de uma venda, atualizando automaticamente a quantidade dos produtos devolvidos em estoque e armazenando a devolução no histórico.

**Requisitos** – RF04, RF02.  
**Ator** – Usuário com permissão para registrar vendas e devoluções.  
**Pré-condição** – Usuário logado no sistema; usuário possui permissão para registrar vendas e devoluções; venda cadastrada no sistema.  
**Pós-condição** – Devolução registrada no histórico; quantidade dos produtos devolvidos adicionada ao estoque.

### **Fluxo principal**

1. O usuário solicita **Registrar devolução**.  
2. O sistema solicita o ID da venda que será devolvida.  
3. O usuário informa o ID da venda.  
4. O sistema mostra os produtos pertencentes à venda e as respectivas quantidades. \[UC008.FS001\]  
5. O usuário seleciona os produtos que deseja devolver e informa as respectivas quantidades. \[UC008.FS002\]  
6. O sistema mostra as opções de motivo da devolução, com **Nenhum motivo especificado** selecionado por padrão.  
7. O usuário seleciona o motivo da devolução. Caso selecione **Outro**, informa o motivo em uma caixa de texto. \[UC008.FS003\]  
8. O sistema mostra os produtos, as quantidades devolvidas, o motivo da devolução e solicita a confirmação da operação.  
9. O usuário confirma a devolução.  
10. O sistema registra a devolução como um novo evento no histórico, vinculando-a à venda original.  
11. O sistema adiciona ao estoque as quantidades dos produtos devolvidos.  
12. O sistema mostra uma mensagem dizendo que a devolução foi registrada com sucesso.

### **Fluxos alternativos**

**\[UC008.FS001\] – Venda não encontrada**

1. O usuário solicita **Registrar devolução**.  
2. O sistema solicita o ID da venda que será devolvida.  
3. O usuário informa um ID de venda não cadastrado no sistema.  
4. O sistema mostra uma mensagem informando que a venda não foi encontrada.  
5. O sistema retorna à interface solicitando o ID da venda.

**\[UC008.FS002\] – Quantidade devolvida superior à quantidade vendida**

1. O usuário solicita **Registrar devolução**.  
2. O sistema solicita o ID da venda que será devolvida.  
3. O usuário informa o ID da venda.  
4. O sistema mostra os produtos pertencentes à venda e as respectivas quantidades.  
5. O usuário informa uma quantidade de devolução superior à quantidade correspondente na venda.  
6. O sistema mostra uma mensagem informando que a quantidade devolvida não pode ser superior à quantidade vendida.  
7. O sistema retorna à interface para seleção dos produtos e quantidades.

**\[UC008.FS003\] – Motivo "Outro" sem descrição**

1. O usuário solicita **Registrar devolução**.  
2. O sistema solicita o ID da venda que será devolvida.  
3. O usuário informa o ID da venda.  
4. O sistema mostra os produtos pertencentes à venda.  
5. O usuário seleciona os produtos e informa as quantidades a serem devolvidas.  
6. O sistema mostra as opções de motivo da devolução.  
7. O usuário seleciona **Outro**.  
8. O sistema mostra uma caixa de texto para descrição do motivo.  
9. O usuário não informa o motivo e tenta prosseguir.  
10. O sistema mostra uma mensagem solicitando que o motivo seja informado.  
11. O usuário informa o motivo e prossegue com a devolução.

**\[UC008.FS004\] – Cancelar devolução**

1. O usuário solicita **Registrar devolução**.  
2. O sistema solicita o ID da venda que será devolvida.  
3. O usuário informa o ID da venda.  
4. O sistema mostra os produtos pertencentes à venda.  
5. O usuário seleciona os produtos, informa as quantidades e seleciona o motivo da devolução.  
6. O sistema mostra os dados da devolução e solicita a confirmação.  
7. O usuário cancela a operação.  
8. O sistema cancela a devolução sem registrar o evento e sem alterar o estoque.

* ## **UC009** – Registrar carregamento

**Descrição** – Registrar a entrada de um ou mais produtos no estoque, armazenando o carregamento em seu respectivo histórico.

**Requisitos** – RF05, RF02.  
**Ator** – Usuário com permissão para registrar carregamentos.  
**Pré-condição** – Usuário logado no sistema; usuário possui permissão para registrar carregamentos; produtos cadastrados no sistema.  
**Pós-condição** – Carregamento registrado no histórico de carregamentos; quantidades dos produtos adicionadas ao estoque.

### **Fluxo principal**

1. O usuário solicita **Registrar carregamento**.  
2. O sistema mostra uma interface para selecionar os produtos do carregamento.  
3. O usuário pesquisa um produto pelo ID ou o seleciona na lista. \[UC009.FS001\]  
4. O sistema adiciona o produto ao carregamento.  
5. O usuário informa a quantidade recebida e a fonte do produto.  
6. O usuário adiciona outros produtos ao carregamento, repetindo os passos 3 a 5, caso necessário.  
7. O sistema mostra os produtos selecionados, suas quantidades e respectivas fontes, além da data e hora do carregamento e do usuário responsável pelo registro.  
8. O usuário solicita o registro do carregamento.  
9. O sistema solicita a confirmação do registro.  
10. O usuário confirma o registro.  
11. O sistema registra o carregamento no histórico de carregamentos.  
12. O sistema adiciona ao estoque as quantidades dos produtos recebidos.  
13. O sistema mostra uma mensagem dizendo que o carregamento foi registrado com sucesso.

### **Fluxos alternativos**

**\[UC009.FS001\] – Produto não encontrado**

1. O usuário solicita **Registrar carregamento**.  
2. O sistema mostra uma interface para selecionar os produtos do carregamento.  
3. O usuário pesquisa um produto pelo ID.  
4. O sistema não encontra nenhum produto correspondente.  
5. O sistema mostra uma mensagem informando que o produto não foi encontrado.  
6. O sistema retorna à interface de seleção de produtos.

**\[UC009.FS002\] – Quantidade inválida**

1. O usuário solicita **Registrar carregamento**.  
2. O sistema mostra uma interface para selecionar os produtos do carregamento.  
3. O usuário seleciona um produto.  
4. O usuário informa uma quantidade inválida.  
5. O sistema mostra uma mensagem informando que a quantidade deve ser válida.  
6. O sistema retorna à interface para informar a quantidade.

**\[UC009.FS003\] – Fonte não informada**

1. O usuário solicita **Registrar carregamento**.  
2. O sistema mostra uma interface para selecionar os produtos do carregamento.  
3. O usuário seleciona um produto e informa a quantidade recebida.  
4. O usuário não informa a fonte do produto.  
5. O sistema mostra uma mensagem solicitando que a fonte seja informada.  
6. O usuário informa a fonte e prossegue com o registro.

**\[UC009.FS004\] – Cancelar carregamento**

1. O usuário solicita **Registrar carregamento**.  
2. O sistema mostra uma interface para registrar o carregamento.  
3. O usuário seleciona os produtos, informa suas quantidades e respectivas fontes.  
4. O usuário solicita o cancelamento do carregamento.  
5. O sistema informa que o cancelamento descartará as informações inseridas e solicita confirmação.  
6. O usuário confirma o cancelamento.  
7. O sistema cancela a operação sem registrar o carregamento e sem alterar o estoque.

* ## **UC010** – Consultar receita por janela de tempo

**Descrição** – Consultar a receita total de vendas e devoluções em uma determinada janela de tempo, apresentando a movimentação diária em um gráfico.

**Requisitos** – RF06, RF02.  
**Ator** – Usuário com permissão para consultar receitas.  
**Pré-condição** – Usuário logado no sistema; usuário possui permissão para consultar receitas.

**Pós-condição** – Receita de vendas, devoluções e resultado do período exibidos ao usuário.

### **Fluxo principal**

1. O usuário solicita **Consultar receita**.  
2. O sistema solicita a data inicial e a data final da janela de tempo.  
3. O usuário informa as datas e solicita a consulta.  
4. O sistema busca as vendas e devoluções realizadas dentro da janela de tempo.  
5. O sistema apresenta um gráfico contendo, para cada dia do período, as vendas e as devoluções.  
6. O sistema apresenta a receita total de vendas, o valor total das devoluções e o resultado do período.  
7. O sistema mantém os resultados exibidos enquanto o usuário permanecer na consulta.

### **Fluxos alternativos**

**\[UC010.FS001\] – Data inicial posterior à data final**

1. O usuário solicita **Consultar receita**.  
2. O sistema solicita a data inicial e a data final.  
3. O usuário informa uma data inicial posterior à data final.  
4. O sistema mostra uma mensagem informando que a data inicial deve ser anterior ou igual à data final.  
5. O sistema retorna à interface de seleção da janela de tempo.

**\[UC010.FS002\] – Cancelar consulta**

1. O usuário solicita **Consultar receita**.  
2. O sistema solicita a data inicial e a data final.  
3. O usuário cancela a consulta.  
4. O sistema cancela a operação e retorna à interface anterior.
