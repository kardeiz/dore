module Dore
  class BitstreamsController < ApplicationController
  
    before_action do
      @context = Utility.get_context(session[:user_id])
      @bitstream = Bitstream.find_for_read(@context, params[:id])
    end  
    
    def retrieve
      send_data @bitstream.read, :type => @bitstream.mime_type, :disposition => 'inline'
    end
  
  end
end
