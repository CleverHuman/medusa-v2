### Medusa 
- vue2 
- spring boot

## System  Structure
![img.png](img.png)


- medusa-admin-vue
- medusa-admin
- medusa-mall

## git config
### 1.add host config
vim ~/.ssh/config

```text
Host gitea-onion
    HostName vsfiu4u3z4zsa36s5uwjhot6tkjekmjhati6ix5mdeztbiotxtj5nfad.onion
    User git
    IdentityFile ~/.ssh/gitea_key
    Port 2222
    ProxyCommand socat - SOCKS4A:localhost:%h:%p,socksport=9050
    ConnectTimeout 30
    ConnectTimeout 30
    ServerAliveInterval 15
    ServerAliveCountMax 3
    IdentitiesOnly yes
    LogLevel DEBUG3  # 启用详细日志

```
### 2. generate ssh key
https://docs.github.com/en/authentication/connecting-to-github-with-ssh
https://docs.github.com/en/authentication/connecting-to-github-with-ssh/generating-a-new-ssh-key-and-adding-it-to-the-ssh-agent
![gitea_ssh.png](gitea_ssh.png)


### 3.add gitea sshkey
vim ~/.ssh/gitea_key
put in the ssh key 

### git clone 
git clone git@gitea-onion:admin_root/medusa_v2.git




## env setting
### local
docker compose -f docker-compose.local.yml --env-file .env.local up -d --build
docker compose -f docker-compose.local.yml --env-file .env.local down -v

### dev
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d --build
docker compose -f docker-compose.dev.yml --env-file .env.dev down -v

### staging
docker compose -f docker-compose.staging.yml --env-file .env.staging up -d --build
docker compose -f docker-compose.staging.yml --env-file .env.staging down -v

### prod
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml --env-file .env.prod down -v
