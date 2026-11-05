# Bestand

Aplicativo desktop para gerenciamento de estoque, vendas, fornecedores e relatórios, desenvolvido em Java com JavaFX e SQLite.

## Requisitos

- JDK 25
- Apache Maven 3.8 ou superior

## Executar

Na pasta raiz do projeto:

```bash
mvn clean javafx:run
```

O aplicativo usa o arquivo `bestand.db` na pasta de execução. Se o banco ainda não existir, as tabelas necessárias serão criadas ao iniciar.

## Compilar

```bash
mvn clean package
```

O artefato compilado é gerado em `target/`.

## Estrutura

```text
src/
  main/
    java/br/com/bestand/
      database/   Acesso e inicialização do SQLite
      ui/         Telas do aplicativo
      App.java    Ponto de entrada
    resources/    Estilos
bestand.db        Banco de dados local
pom.xml           Configuração Maven e dependências
```
