@Library('hawaii') _

//Declarative pipeline
pipeline {
	agent {
		node {
			label 'windows'
		}
	}
	
	tools {
		jdk "JDK openjdk 1.8"
		maven "Maven 3.0.4"
	}
	
	//Declaration des options (connexion gitlab, configuration...)
	options {
		disableConcurrentBuilds()
		gitLabConnection('gitlab-jenkins')
		gitlabBuilds(builds: ['test&coverage', 'build', 'deploy'])
	}
	
	triggers {
		gitlab(
			triggerOnPush: true,
			triggerOnMergeRequest: true,
			skipWorkInProgressMergeRequest: true,
			triggerOpenMergeRequestOnPush: 'both',
			acceptMergeRequestOnSuccess: false,
			branchFilterType: "RegexBasedFilter",
			targetBranchRegex: ".*release.*|.*feature.*|.*hotfix.*"
		)
	}
	
	stages {
		stage('test&coverage') {
			steps {
				utilsExecuteCommand(cmd: "mvn clean verify")
				updateGitLabCommitStatus name: 'test&coverage', state: 'success'
			}
		}
		
		stage('build') {
			steps {
				utilsExecuteCommand(cmd: "mvn package -Dmaven.test.skip=true")
				updateGitLabCommitStatus name: 'build', state: 'success'
			}
		}
		
		stage('deploy') {
			when {
				expression { env.GIT_BRANCH?.startsWith('origin/release') || env.GIT_BRANCH?.startsWith('origin/hotfix') }
			}
			steps {
				script {
					utilsExecuteCommand(cmd: "mvn source:jar deploy -Dmaven.test.skip=true -Dmaven.install.skip=true")
					gitlabSetTag()
				}
			}
		}
	}
}
