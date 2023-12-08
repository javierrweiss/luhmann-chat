FROM gitpod/workspace-full

USER gitpod

RUN wget -O Mambaforge.sh "https://github.com/conda-forge/miniforge/releases/latest/download/Mambaforge-$(uname)-$(uname -m).sh" \
 && bash  Mambaforge.sh -b -p "${HOME}/conda" && rm -f Mambaforge.sh \
 && . "${HOME}/conda/etc/profile.d/conda.sh" \
 && . "${HOME}/conda/etc/profile.d/mamba.sh" 
