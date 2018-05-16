FROM python:3

WORKDIR /

ADD build/python-build python-build
ADD etc etc
ADD src/__main__.py .

RUN python -m pip install --upgrade pip && \
    pip install python-build/*.whl && \
    apt-get update -y && \
    apt-get install -y openssh-server && \
    echo 'root:root' | chpasswd && sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    apt-get clean

CMD ["python", "/"]
