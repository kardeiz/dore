module Dore
  class Bitstream < SimpleDelegator

  
    def self.find_for_read(context, id = nil)
      new(org.dspace.content.Bitstream.find(context, id.to_i).tap do |bs|
        Utility.authorize(context, :READ, bs)
      end)
    end
  
    def mime_type
      get_format.get_mime_type
    end
  
    def read
      retrieve.to_io.read
    end
  
  end
end
