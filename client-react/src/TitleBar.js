function TitleBar(props) {
	const { accounts, setSelectedAccount, portfolio } = props;

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});

	return (
		<div className="pm-block">
			<div style={{ display: "flex", alignItems: "center" }}>
				<div style={{ marginRight: 12 }}>
					<select name="accounts" id="accounts" onChange={(e) => setSelectedAccount(e.target.value)}>
						{accountOptions}
					</select>
				</div>

				{
					portfolio && (
						<div>
							<span style={{ marginRight: 12 }}>Balance: {portfolio.balance.toLocaleString()}</span>
							<span style={{ marginRight: 12 }}>Cash: {portfolio.cash.toLocaleString()}</span>
						</div>
					)
				}
			</div>
		</div>
	);
}

export default TitleBar;
