module Dore
  class BitstreamsService < BaseService
  
    def bitstream
      return unless id = params[:id]
      Utility.find_bitstream(context, id)
    end
    
  end
end

