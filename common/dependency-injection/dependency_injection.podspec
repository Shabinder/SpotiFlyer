Pod::Spec.new do |spec|
    spec.name                     = 'dependency_injection'
    spec.version                  = '1.0'
    spec.homepage                 = 'https://github.com/Shabinder/SpotiFlyer'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'Shabinder Singh'
    spec.license                  = ''
    spec.summary                  = 'SpotiFlyer Native Module'

    spec.static_framework         = true
    spec.vendored_frameworks      = "build/cocoapods/framework/SpotiFlyer.framework"
    spec.libraries                = "c++"
    spec.module_name              = "#{spec.name}_umbrella"

    spec.ios.deployment_target = '13.5'

    spec.dependency 'TagLibIOS', '~> 0.3'

    spec.pod_target_xcconfig = {
        'KOTLIN_TARGET[sdk=iphonesimulator*]' => 'ios_x64',
        'KOTLIN_TARGET[sdk=iphoneos*]' => 'ios_arm',
        'KOTLIN_TARGET[sdk=watchsimulator*]' => 'watchos_x64',
        'KOTLIN_TARGET[sdk=watchos*]' => 'watchos_arm',
        'KOTLIN_TARGET[sdk=appletvsimulator*]' => 'tvos_x64',
        'KOTLIN_TARGET[sdk=appletvos*]' => 'tvos_arm64',
        'KOTLIN_TARGET[sdk=macosx*]' => 'macos_x64'
    }

    spec.script_phases = [
        {
            :name => 'Build dependency_injection',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../gradlew" -p "$REPO_ROOT" :common:dependency-injection:syncFramework \
                    -Pkotlin.native.cocoapods.target=$KOTLIN_TARGET \
                    -Pkotlin.native.cocoapods.configuration=$CONFIGURATION \
                    -Pkotlin.native.cocoapods.cflags="$OTHER_CFLAGS" \
                    -Pkotlin.native.cocoapods.paths.headers="$HEADER_SEARCH_PATHS" \
                    -Pkotlin.native.cocoapods.paths.frameworks="$FRAMEWORK_SEARCH_PATHS"
            SCRIPT
        }
    ]
end