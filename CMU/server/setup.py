from setuptools import setup

setup(
    name='p2server',
    packages=['p2server'],
    include_package_data=True,
    install_requires=[
        'flask', 'Flask-SQLAlchemy',
    ],
)