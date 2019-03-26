javapackager -deploy -native image -outdir packages -outfile VJvmSandbox -srcdir . -srcfiles VJvmSendBox.jar -appclass ooo.reindeer.jvm.sandbox.view.Main
xcopy /E /y lib packages\bundles\Main\app\lib\
xcopy /E /y sandbox packages\bundles\Main\app\sandbox\
rmdir /Q /S lib sandbox
del /Q VJvmSendBox.*
xcopy /E /y packages\bundles\Main .\
rmdir /Q /S packages
del /Q  buildFXRuntime.bat