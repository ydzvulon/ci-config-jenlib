
// @@act=declare reflection.name=as-yaml python=it.splint("_")[1].split(^^):

def kws = [
	//
	jenlib_name: 'JenkinsLib_jenlib',
	jenlib_version: 'main.ydzvulon',
	jenlib_remote: 'https://github.com/ydzvulon/jenlib.git',
	repo_path: "${seed_job_repo}",
	seed_job_branch: "${seed_job_branch}",
	root_dir: "samples2",
	seed_job_pipes: [
		'_cicd/batches/a_simple_batch/pipe_500_cmds_minimal.groovy',
		'_cicd/batches/a_simple_batch/pipe_501_cmds_explicit.groovy',
		'_cicd/batches/a_simple_batch/pipe_502_tasks_explicit.groovy',
		'_cicd/batches/a_simple_batch/pipe_503_tasks_from_yml.groovy',
		'_cicd/batches/c_complex_batch/pipe_701_complex_pipes.groovy',
		'_cicd/batches/c_complex_batch/pipe_703_tasks_from_yml.groovy',
		'_cicd/batches/c_complex_batch/pipe_730_groovy_dynamic.groovy',
	]
]


// folder(kws.root_dir){
// }

folder(kws.root_dir) {
  properties {
    folderLibraries {
      libraries {
        libraryConfiguration {
          name(kws.jenlib_name)
          defaultVersion(kws.jenlib_version)
          implicit(false)
          allowVersionOverride(true)
          includeInChangesets(true)
          retriever {
            scmSourceRetriever {
              scm {
                git {
                  remote(kws.jenlib_remote)
                //   credentialsId('some-credentials')
                }
              }
            }
          }
        }
      }
    }
  }
  configure { node ->
    def traits = node / 'properties' / 'org.jenkinsci.plugins.workflow.libs.FolderLibraries' / 'libraries' / 'org.jenkinsci.plugins.workflow.libs.LibraryConfiguration' / 'retriever' / 'scm' / 'traits'
    traits << { 'jenkins.plugins.git.traits.BranchDiscoveryTrait'() }
  }
}

for (seed_job_pipe in kws.seed_job_pipes)
{
	def arr = seed_job_pipe.split('/')
	def jname = arr[-1]
	def jfname = "${kws.root_dir}/${jname}"
	pipelineJob(jfname) {
		description()
		keepDependencies(false)
		definition {
			cpsScm {
				scm {
					git {
						remote {
							url(kws.repo_path)
						}
						branch("*/${kws.seed_job_branch}")
					}
				}
				scriptPath("${seed_job_pipe}")
			}
		}
		// environmentVariables {
		// 	envs(jobs_def_dir: "\${jobs_def_dir}")
		// 	keepBuildVariables(true)
		// }
		disabled(false)
		configure {
			it / 'properties' / 'com.sonyericsson.rebuild.RebuildSettings' {
				'autoRebuild'('false')
				'rebuildDisabled'('false')
			}
		}
	}
}