module Dore
  class Engine < ::Rails::Engine
    isolate_namespace Dore
    
    initializer 'load.settings' do |app|
      initialize_settings app.root.join('config/initializers/dore.yml')
    end
    
    initializer 'load.jars' do
      Dir.glob( File.join self.config.dspace_libs, '*.jar' ).each do |jar|
        require jar
      end
    end
    
    initializer 'load.dspace_cfg' do
      org.dspace.core.ConfigurationManager.load_config config.dspace_cfg
    end
    
    initializer 'start.dspace.kernel' do
      kernel_impl = org.dspace.servicemanager.DSpaceKernelInit.get_kernel(nil)
      dspace_dir = org.dspace.core.ConfigurationManager.get_property('dspace.dir')
      kernel_impl.start(dspace_dir) unless kernel_impl.is_running
    end
    
    initializer 'add.custom.java.to.classpath' do
      $CLASSPATH << self.class.root.join('lib/dore/java').to_s
    end
    
    private
    
    def initialize_settings(config_file)
      yaml = YAML.load_file config_file
      config.dspace_cfg  = yaml['dspace_cfg']
      config.dspace_libs = yaml['dspace_libs'] || begin
        File.expand_path '../../lib', config.dspace_cfg
      end
    end
    
  end
end
