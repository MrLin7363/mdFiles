sudo iptables -A INPUT -s 7.256.zz.zzz -j DROP
sudo iptables -A INPUT -s 7.256.zz.zz -j DROP

解除限制
sudo iptables -D INPUT -s x.x.x.x -j DROP
 
sudo iptables-save
