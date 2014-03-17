require 'bootstrap-sass'
require 'font-awesome-sass'
require 'jquery-rails'
require 'dore/engine'
require 'dore/utility'

module Dore

  def self.config(&block)
    yield Engine.config if block
    Engine.config
  end

  def self.new_context
    org.dspace.core.Context.new
  end

end
