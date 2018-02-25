# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty64"

	config.vm.provision "shell", inline: <<-SHELL
		apt-get -y update

		# Basic essential toolkit
		apt-get -y install wget
		apt-get -y install build-essential
		apt-get -y install python-dev
		apt-get -y install python-pip
		easy_install -U pip

		echo "[DroneKit]: Installing JDK 7"
		if [[ -z `which 'java'` ]]; then
			apt-get -y install openjdk-7-jre-headless
			if grep -v JAVA_HOME "/etc/environment"; then
				echo "JAVA_HOME='/usr/lib/jvm/java-7-openjdk-amd64/'" >> /etc/environment
			fi
		else
			echo "[DroneKit]: Already installed JDK 7"
		fi
		source /etc/environment

		echo "[DroneKit]: Installing gradle"
		if [[ -z `which 'gradle'` ]]; then
			add-apt-repository ppa:cwchien/gradle
			apt-get -y update
			apt-get -y install gradle-2.2.1
		else
			echo "[DroneKit]: Already installed gradle"
		fi

		echo "[DroneKit]: Android SDK"
		if [[ -z `which 'ubuntu-sdk'` ]]; then
			wget http://dl.google.com/android/android-sdk_r24.1.2-linux.tgz
			tar -zxvf android-sdk_r24.1.2-linux.tgz
			export ANDROID_HOME=$HOME/android-sdk-linux
			echo "`PATH DEFAULT=${PATH}:/home/vagrant/android-sdk-linux/tools`" >> ~/.bashrc
			echo "`PATH DEFAULT=${PATH}:/home/vagrant/android-sdk-linux/platform-tools`" >> ~/.bashrc
			cd $ANDROID_HOME
			#./tools/android update -s --no-gui
			( sleep 5 && while [ 1 ]; do sleep 1; echo y; done ) | ./tools/android update sdk --no-ui --filter platform-tool,android-21
			cd /home/vagrant
		else
			echo "[DroneKit]: Already installed Android SDK"
		fi

		echo "[DroneKit]: Sphinx"
		pip install sphinx
		pip install sphinx_3dr_theme
		pip install -U sphinx_3dr_theme

		cd /vagrant

		echo "[DroneKit]: Java Docs"
		# The javadocs will be generated in the '<top_directory>/ClientLib/mobile/build/docs/javadoc' directory.
		./gradlew :ClientLib:mobile:clean :ClientLib:mobile:androidJavadocs

		#echo "[DroneKit]: Sphinx Docs "
		cd /vagrant/doc
		make clean
		make html
	SHELL
end
