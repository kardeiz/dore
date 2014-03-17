$:.push File.expand_path("../lib", __FILE__)

# Maintain your gem's version:
require "dore/version"

# Describe your gem and declare its dependencies:
Gem::Specification.new do |s|
  s.name        = "dore"
  s.version     = Dore::VERSION
  s.authors     = ["Jacob Brown"]
  s.email       = ["kardeiz@gmail.com"]
  s.homepage    = "https://github.com/kardeiz/dore"
  s.summary     = "Summary of Dore."
  s.description = "Description of Dore."

  s.files = Dir["{app,config,db,lib}/**/*", "MIT-LICENSE", "Rakefile", "README.rdoc"]
  s.test_files = Dir["test/**/*"]

  s.add_dependency "rails", "~> 4.0.3"
  s.add_dependency "bootstrap-sass"
  s.add_dependency "font-awesome-sass"
  s.add_dependency "sass-rails"
  s.add_dependency "jquery-rails"
  s.add_development_dependency "jquery-rails"
  s.add_development_dependency "therubyrhino"
  s.add_development_dependency "puma"
  s.add_development_dependency "rack"
end
