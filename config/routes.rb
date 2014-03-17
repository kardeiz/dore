Dore::Engine.routes.draw do

  resources :items
  resources :communities
  resources :collections
  
  resources :bitstreams do
    get :retrieve, :on => :member
  end

  get 'search', :to => 'static#search'

  root :to => 'static#home'

end
