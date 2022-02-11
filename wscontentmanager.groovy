@Library('hawaii') _

// Déclaration de la pipeline
pipeline {
    //Définition de l'agent
    agent any
    
    //Indication des outils de développement
    tools {
        jdk "JDK sun 1.8"
        maven "Maven 3.2.5"
    }

    // Déclaration des options (connexion gitlab, configuration...)
    options {
        disableConcurrentBuilds()                         //Désactivation du build dans GitLab
        gitLabConnection('gitlab-jenkins')                //Connexion à GitLab
        gitlabBuilds(builds: ['test&coverage', 'build'])  //Etapes à suivre
    }
    
    //Condition d'exécution de la pipeline
    //Exécuter quand il y a un push ou un merge dans GitLab si cela viens des branches : release ou feature ou hotfix
    triggers {
        gitlab(
          triggerOnPush: true,
          triggerOnMergeRequest: true,
          skipWorkInProgressMergeRequest: true,
          triggerOpenMergeRequestOnPush: 'both',
          acceptMergeRequestOnSuccess: false,
          branchFilterType: "RegexBasedFilter",
          targetBranchRegex: ".*release/.*|.*feature/.*|*.hotfix/*"
        )
    }

    //Etapes
    stages {
        // Tests unitaires et couverture de code
        stage('test&coverage') {
                steps {
                    utilsExecuteCommand(cmd: "mvn clean verify -Pcoverage")           //Maven "clean and build" + tests unitaires
                    updateGitlabCommitStatus name: 'test&coverage', state: 'success'  //Affichage du résultat "test&coverage" dans le tableau de résultat des étapes
                }
        }
	// Build
        stage('build') {
            steps {
                utilsExecuteCommand(cmd: "mvn package -Dmaven.test.skip=true")        //Build Maven
                updateGitlabCommitStatus name: 'build', state: 'success'              //Affichage du résultat "build" dans le tableau de résultat des étapes
            }
        }
		// Pour l'instant en commentaire car le déploiement n'est pas encore à l'ordre du jour
		//Déploiement
		//stage('deploy') {                                                                                                 // Si ("success" / true dans les étapes précedentes)
		//	when {                                                                                                          // Si
		//		expression { env.GIT_BRANCH?.startsWith('origin/release') || env.GIT_BRANCH?.startsWith('origin/hotfix') }    // La branche est release ou hotfix
		//	}
        //    steps {
        //        utilsExecuteCommand(cmd: "mvn source:jar deploy -DskipTests -Dmaven.install.skip=true")               // Déploiement sur le serveur
		//		gitlabSetTag()
        //        updateGitlabCommitStatus name: 'deploy', state: 'success'                                             // Affichage du résultat "deploy" dans le tableau de résultat des étapes
        //    }
        //}
		
    }

    post {
        success {
            junit "target/surefire-reports/*.xml"
        }
    }
}
