import os

from setuptools import setup, find_packages

projProperties = {
    'name': 'example.mesos.executor',
    'version': '0.0.0'
}

src_path = os.path.abspath('src')

with open("requirements.txt") as resource:
    install_requires = resource.readlines()

setup(
    name=projProperties['name'],
    version=projProperties['version'],
    description='Mesos executor example',
    packages=find_packages(where=src_path),
    package_dir={'': 'src'},
    package_data={'': '*.json'},
    install_requires=install_requires,
    include_package_data=True
)