#!/bin/bash

echo "Running scripts..."
alias python=/shared/scratch/am13743/tf_shared/bin/python3.7
alias python3=/shared/scratch/am13743/tf_shared/bin/python3.7
export PYTHONPATH=/shared/scratch/am13743/tf_shared/lib/python3.7/site-packages/
alias pip=/shared/scratch/am13743/tf_shared/bin/pip

/shared/scratch/am13743/tf_shared/bin/python3.7 scrape.py -p "$id"