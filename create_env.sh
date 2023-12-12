 #!/bin/bash
 
 sudo apt-get install -y libgl1-mesa-glx
 /home/gitpod/conda/bin/conda init bash
 . /home/gitpod/conda/etc/profile.d/conda.sh
 . /home/gitpod/conda/etc/profile.d/mamba.sh   
 pip3 install --user -r requirements.txt   
 conda create -n luhmann --clone /home/gitpod/conda

