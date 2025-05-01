# Jeff's core Java utilities and application support


## Verifying passphrase of existing key pairs

From https://stackoverflow.com/questions/4411457

```
ssh-keygen -y -f <path to key>
```

1. If there is not a passphrase, it won't ask for one; it will show the public key.
2. Otherwise, it will ask for one, and show the public key if you entered it correctly.

## Generating fingerprints (SHA256 hashes)

```
ssh-keygen -lf <path to key>
```

## Setting appropriate file permissions

```
chmod 644 <keyname>.pub
chmod 600 <keyname>
```

## Allowing pushing etc to private github repo that I have the key to

1. Make sure the project's remote url has the right protocol (git, not https)

In the project directory (e.g. `java-core`), edit the `.git/config` file.

```
[core]
  repositoryformatversion = 0
  filemode = true
  bare = false
  logallrefupdates = true
  ignorecase = true
  precomposeunicode = true
[remote "origin"]
  url = git@github.com:jpsember/java-core.git           <------ NOTE THIS LINE!!!!
  fetch = +refs/heads/*:refs/remotes/origin/*
[branch "master"]
  remote = origin
  merge = refs/heads/master
```
Note the format of that url:

```url = git@github.com`:<user name>/<repo name>.git```


2. Make sure the key has been added to apple's keychain or whatever


See: https://stackoverflow.com/questions/21095054/ssh-key-still-asking-for-password-and-passphrase

```
ssh-add --apple-use-keychain ~/.ssh/<keyname>
```
