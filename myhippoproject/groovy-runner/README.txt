Groovy Runner
=============

Deze runner zorgt ervoor dat Groovy scripts worden opgestart in het CMS. In de property file runner.properties kan worden aangegeven welke scripts worden opgestart.
De runner maakt connectie via RMI met de repository, en kopieert vervolgens de groovy scripts naar de queue. Het CMS voert dan de scripts uit. De runner checkt of de
scripts zijn uitgevoerd dmv de property wait.until.done. De maximale tijd die de runner blijft pollen kan worden geconfigureerd max.sleep.interval (in minuten).

* Bouwen *

Je kunt deze module bouwen met:

mvn clean package

* Lokaal runner starten *

Met behulp van de exec-maven-plugin kan de runner lokaal gestart worden:

mvn exec:java -Dexec.args=/path/to/runner.properties

of ./exec.sh

1. Let op dat de paden goed staan in exec.sh
2. Geef in het gebruikte runner properties bestand een geldige username en wachtwoord op.
3. RMI moet aan staan.


* Packagen *

Je kunt een app + tar.gz bouwen m.b.v. een profiel. Gebruik daarvoor de volgende commando:

mvn clean package -Passembletars

De tar.gz komt nu in de target folder van de runner te staan.



LET OP WINDOWS GEBRUIKERS! Als de volgende foutmelding optreed in de CMD dan is de classpath te lang in jcr-runner.bat.
- The input line is too long.
- The syntax of the command is incorrect.

Run de runner in de root folder (voorbeeld C:\jcr-runner)