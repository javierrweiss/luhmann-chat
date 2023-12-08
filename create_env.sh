 #!/bin/bash
 
 source /home/gitpod/conda/etc/profile.d/conda.sh
 source /home/gitpod/conda/etc/profile.d/mamba.sh   
 pip3 install --user -r requirements.txt   
 conda create -n luhmann --clone /home/gitpod/conda

