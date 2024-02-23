# Requisição web service REST para consulta externa
---


Para compilar, é preciso realizar através do Maven
### Passo - 1
Instalar o Maven na máquina local
O Maven não é uma instalação, é somente um pacote.
https://maven.apache.org/download.cgi
Colocar o Maven nas variáveis de ambiente do windows


### Passo - 2
Acessar a pasta aonde está o pom.xml através do cmd
Rodar: mvn clean install
Vai gerar o .war

### Passo - 3
Acessar a central de componentes do fluig, e realizar o deploy por lá inserindo o arquivo .war em target\WSRestOAuth-1.0.war
