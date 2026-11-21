# Guide : Atelier 3 + Homework Jenkins (partie back)

> 📸 = capture d'écran à prendre pour le rendu PDF. Nomme-les `01.png`, `02.png`… dans l'ordre.

---

## PARTIE A : Atelier 3 (installer et configurer Jenkins)

### A0. Démarrer la VM
Dans PowerShell (dossier qui contient ton `Vagrantfile` de l'Atelier 1) :
```
vagrant up
vagrant ssh
```
Récupère l'IP de la VM (souvent `192.168.33.10`, regarde `config.vm.network` dans le Vagrantfile) :
```
ip a
```

### A1. Java JDK 21
```
sudo apt update
sudo apt install openjdk-21-jdk -y
java -version
sudo nano /etc/environment      # ajouter : JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64/"
source /etc/environment
echo $JAVA_HOME
```
📸 **01** : `java -version` + `echo $JAVA_HOME`

### A2. Maven
```
sudo apt install maven -y
mvn -version
sudo nano /etc/environment      # ajouter : M2_HOME="/usr/share/maven"
source /etc/environment
echo $M2_HOME
```
📸 **02** : `mvn -version` + `echo $M2_HOME`

### A3. Git
```
sudo apt install git -y
git --version
```
📸 **03** : `git --version`

### A4. Jenkins
```
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 5BA31D57EF5975CA
sudo apt update
sudo apt install jenkins -y
sudo systemctl start jenkins
sudo systemctl enable jenkins
sudo systemctl status jenkins
```
> Si `apt update` affiche `NO_PUBKEY 7198F4B714ABFC68` / `Package 'jenkins' has no installation candidate` : Jenkins a changé sa clé de signature. Ajoute la nouvelle clé puis relance :
> ```
> sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 7198F4B714ABFC68
> sudo apt update && sudo apt install jenkins -y
> ```

> Si `apt-key` donne une erreur (Ubuntu 24.04), utilise la méthode officielle à la place :
> ```
> sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
> echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list
> sudo apt update && sudo apt install jenkins -y
> ```

📸 **04** : `systemctl status jenkins` affiche **active (running)** en vert

### A5. Premier accès dans le navigateur (Windows)
1. Ouvre `http://IP_DE_LA_VM:8080`
2. Dans la VM : `sudo cat /var/lib/jenkins/secrets/initialAdminPassword`, puis copie-colle le mot de passe
3. **Installer les plugins suggérés**, puis attends la fin
4. Crée l'admin (ou continue avec admin), garde l'URL `http://IP:8080/`

📸 **05** : page d'installation des plugins  
📸 **06** : tableau de bord Jenkins (Bienvenue)

### A6. Configurer les outils : *Administrer Jenkins > Tools*
- **JDK** > Ajouter JDK : Nom `JAVA_HOME`, chemin `/usr/lib/jvm/java-21-openjdk-amd64/`, décocher *Install automatically*
- **Maven** > Ajouter Maven : Nom `M2_HOME`, chemin `/usr/share/maven`, décocher *Install automatically*
- **Git** : rien à faire (détecté automatiquement)
- **Apply**, puis **Save**

⚠️ Les noms `JAVA_HOME` et `M2_HOME` doivent être **exactement** ceux-là : le `Jenkinsfile` les utilise.

📸 **07** : section JDK remplie  
📸 **08** : section Maven remplie

✅ Atelier 3 terminé.

---

## PARTIE B : Homework (pipeline back-end)

Fichiers déjà ajoutés au projet :
| Fichier | Rôle |
|---|---|
| `Jenkinsfile` (racine) | Pipeline : checkout, tests, package, email si échec |
| `backend/src/test/java/.../EquipeServiceImplTest.java` | 5 tests unitaires (Mockito) |
| `backend/src/test/resources/application.properties` | Les tests utilisent H2 en mémoire, donc **pas besoin de MySQL dans la VM** |
| `backend/pom.xml` | Ajout de la dépendance `h2` (scope test) |

> Sans le fichier de test H2, le test `contextLoads()` essaie de se connecter à MySQL `localhost:3306` et **échoue dans Jenkins**.

### B1. Ouvrir le Jenkinsfile et mettre ton email
Dans `Jenkinsfile`, remplace `votre.adresse@gmail.com` par ton adresse.

### B2. Pousser le projet sur TON GitHub
1. Sur github.com : **New repository**, nom `DevOps-AppGestionDesProjets`, **Public**, sans README
2. Dans PowerShell, dans le dossier du projet (celui qui contient `backend/`, `frontend/`, `Jenkinsfile`) :
```
git init
git add .
git commit -m "Projet + Jenkinsfile"
git branch -M main
git remote add origin https://github.com/TON_USER/DevOps-AppGestionDesProjets.git
git push -u origin main
```
(Optionnel : supprime le dossier dupliqué `backend/backend/` avant le commit, c'est une copie inutile.)

📸 **09** : le repo GitHub avec le `Jenkinsfile` visible

### B3. Rendre Jenkins accessible à GitHub (ngrok)
GitHub ne peut pas joindre l'IP privée de ta VM, donc on crée un tunnel public avec **ngrok**.
1. Crée un compte gratuit sur ngrok.com et copie ton **authtoken** (Dashboard > Your Authtoken)
2. Dans la VM :
```
sudo snap install ngrok
ngrok config add-authtoken TON_TOKEN
ngrok http 8080
```
3. Note l'URL `https://xxxx.ngrok-free.app` (laisse ce terminal ouvert ; ouvre un 2ᵉ `vagrant ssh` si besoin)

📸 **10** : terminal ngrok avec la ligne *Forwarding https://… -> http://localhost:8080*

> L'URL ngrok gratuite change à chaque redémarrage, et il faut alors mettre à jour le webhook.

### B4. Créer le webhook GitHub
Repo GitHub > **Settings > Webhooks > Add webhook**
- Payload URL : `https://xxxx.ngrok-free.app/github-webhook/` (avec le `/` final !)
- Content type : `application/json`
- Événement : *Just the push event*
- **Add webhook**

📸 **11** : webhook créé (coche verte ✔ après le premier ping)

### B5. Configurer l'envoi d'email (Gmail)
1. **Mot de passe d'application Gmail** : active la validation en 2 étapes sur ton compte Google, puis va sur https://myaccount.google.com/apppasswords et crée-en un (nom : Jenkins). Copie le code de 16 caractères.
2. Jenkins > **Administrer Jenkins > System** :
   - **Jenkins Location > System Admin e-mail address** : ton adresse Gmail (obligatoire sinon Gmail refuse)
   - **Extended E-mail Notification** :
     - SMTP server : `smtp.gmail.com`
     - SMTP Port : `465`
     - Avancé > Credentials > Ajouter > *Username with password* (username = ton Gmail, password = le mot de passe d'application) et sélectionne-le
     - Cocher **Use SSL**
     - Default Content Type : `HTML (text/html)`
   - **E-mail Notification** (en bas, plugin Mailer, pour tester) :
     - SMTP server `smtp.gmail.com`, Avancé > *Use SMTP Authentication* (Gmail + mot de passe d'application), *Use SSL*, port `465`
     - Coche **Test configuration by sending test e-mail**, mets ton adresse, puis clique **Test configuration**
   - **Save**

📸 **12** : section Extended E-mail Notification remplie (cache le mot de passe)  
📸 **13** : message *Email was successfully sent*

### B6. Créer le job Pipeline
Jenkins > **Nouveau Item** > nom `Backend-Pipeline` > **Pipeline** > OK
- **Build Triggers** : cocher **GitHub hook trigger for GITScm polling**
- **Pipeline** :
  - Definition : **Pipeline script from SCM**
  - SCM : **Git**
  - Repository URL : `https://github.com/TON_USER/DevOps-AppGestionDesProjets.git`
  - Branch : `*/main`
  - Script Path : `Jenkinsfile`
- **Save**

📸 **14** : config Build Triggers (case GitHub hook cochée)  
📸 **15** : config Pipeline from SCM

### B7. Premier build (manuel)
Clique **Lancer un build** (obligatoire une première fois pour que Jenkins enregistre le trigger).  
Le 1er build est long (Maven télécharge les dépendances).

📸 **16** : *Stage View* tout vert (Checkout GitHub, Tests unitaires, Livrable (target))  
📸 **17** : Console Output avec `Tests run: 5, Failures: 0` et `BUILD SUCCESS`  
📸 **18** : le `.jar` dans *Artefacts* du build, ou dans la VM :
```
ls -l /var/lib/jenkins/workspace/Backend-Pipeline/backend/target/
```
(on doit voir `backend-1.0.0.jar`)  
📸 **19** : page *Test Result* (6 tests, 0 failures)

### B8. Prouver le déclenchement par push
Modifie un petit fichier (ex. `README.md`), puis :
```
git add .
git commit -m "test webhook"
git push
```
Un build démarre **tout seul** quelques secondes après.

📸 **20** : build #2 avec la mention **« Started by GitHub push by TON_USER »**  
📸 **21** : GitHub > Webhooks > Recent Deliveries > réponse **200**

### B9. Prouver l'email en cas d'échec
Casse volontairement un test dans `EquipeServiceImplTest.java` :
```java
assertEquals("DevOps-FAUX", resultat.getNom());
```
puis `git commit -am "test echec"` et `git push`.

📸 **22** : build rouge (stage *Tests unitaires* en échec)  
📸 **23** : l'email **« ECHEC du build Backend-Pipeline #3 »** reçu dans Gmail

Ensuite remets `"DevOps"`, puis commit et push, pour obtenir un build vert à nouveau (📸 **24**, optionnel).

---

## PARTIE C : Faire le PDF
1. Ouvre Word, puis mets un titre (Nom, Classe, « Homework Jenkins »)
2. Pour chaque capture : un petit titre (ex. « Étape 7 : Premier build réussi »), puis colle l'image
3. Ajoute aussi le contenu du `Jenkinsfile` (copier-coller)
4. **Fichier > Enregistrer sous > PDF**

---

## Problèmes fréquents
| Problème | Solution |
|---|---|
| `Tool type "jdk" does not have an install of "JAVA_HOME"` | Le nom dans *Tools* doit être exactement `JAVA_HOME` (et `M2_HOME`) |
| Tests échouent avec erreur MySQL / `Communications link failure` | Vérifie que `backend/src/test/resources/application.properties` est bien poussé sur GitHub |
| `release version 17 not supported` | Le JDK dans Tools doit être le 21 (ou ≥17) |
| Push ne déclenche rien | ngrok fermé ou URL changée ; `/github-webhook/` sans le `/` final ; case *GitHub hook trigger* non cochée ; 1er build manuel non lancé |
| Email non reçu | Mot de passe d'application (pas ton mot de passe Gmail normal) ; *System Admin e-mail address* rempli ; port 465 + SSL ; regarde les Spams |
| `No such DSL method 'emailext'` | Installer le plugin **Email Extension** (Administrer Jenkins > Plugins) |
| `No such DSL method 'githubPush'` | Installer le plugin **GitHub** |
| Build très lent / plante (mémoire) | Donne au moins 2–4 Go de RAM à la VM dans le Vagrantfile (`vb.memory = "4096"`) |
