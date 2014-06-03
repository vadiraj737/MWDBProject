function [XNorm,total] = normalize(x)
XMAT=csvread(x);
XNorm=2*mat2gray(XMAT)-1;
total = size(XNorm);
end
